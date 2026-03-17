# extractor.py
import re
import camelot
import pandas as pd
import tempfile
import os
from normalizer import normalizar_periodo, PeriodoVeda
from typing import Any

# Tablas que NO son de datos de vedas — se ignoran por su contenido
TABLAS_IGNORAR_CONTENIDO = {
    "distribución de vedas",   # tabla 1: mapa
    "simbolo",                 # tabla 22: simbología
}

COLUMN_ALIASES = {
    # Especie
    "especie": "especie",
    "especies": "especie",
    "nombre común": "especie",
    "nombre comun": "especie",
    # Zona
    "zona": "zona",
    "zona de aplicación": "zona",
    "entidad": "zona",
    "estado": "zona",
    # Periodo — todos los formatos encontrados en el PDF
    "periodo": "periodo",
    "período": "periodo",
    "periodo de veda": "periodo",
    "fecha de veda": "periodo",
    "periodo alternativo": "periodo_alt",   # tabla 5, se ignora
    # Camarones — tablas 10, 11 con columnas separadas
    "fecha de inicio de veda": "inicio_directo",
    "fecha de fin de veda": "fin_directo",
    "fechas y zonas fin de veda 2025": "fin_directo",
    "fecha de inicio de veda 2026": "inicio_directo",
}

ZONAS_VALIDAS = {
    "yucatan": "YUCATAN",
    "yucatán": "YUCATAN",
    "campeche": "CAMPECHE",
    "quintana roo": "QUINTANA_ROO",
    "golfo de mexico": "GOLFO_DE_MEXICO",
    "golfo de méxico": "GOLFO_DE_MEXICO",
    "océano pacífico": "OCEANO_PACIFICO",
    "oceano pacifico": "OCEANO_PACIFICO",
    "golfo de california": "GOLFO_DE_CALIFORNIA",
    "aguas continentales": "AGUAS_CONTINENTALES",
}

# Textos que indican veda permanente
VEDA_PERMANENTE = [
    "no se puede capturar",
    "no se puede aprovechar",
    "ningún día del año",
    "ningun dia del año",
]


def extraer_tablas(pdf_bytes: bytes) -> tuple[list[dict], list[str]]:
    with tempfile.NamedTemporaryFile(suffix=".pdf", delete=False) as tmp:
        tmp.write(pdf_bytes)
        tmp_path = tmp.name

    try:
        tables = camelot.read_pdf(tmp_path, pages="all", flavor="lattice")
        if len(tables) == 0:
            tables = camelot.read_pdf(tmp_path, pages="all", flavor="stream")

        filas_resultado = []
        errores = []

        for i, table in enumerate(tables):
            df = table.df
            num_tabla = i + 1

            # Ignorar tablas vacías
            contenido_total = " ".join(df.values.flatten()).strip()
            if not contenido_total:
                continue

            # Ignorar tablas de mapa/simbología por su contenido
            if any(keyword in contenido_total.lower() for keyword in TABLAS_IGNORAR_CONTENIDO):
                continue

            df_norm = normalizar_encabezados(df)
            if df_norm is None:
                errores.append(f"Tabla {num_tabla}: no se reconocieron encabezados")
                continue

            for _, row in df_norm.iterrows():
                fila = procesar_fila(row, errores)
                if fila:
                    filas_resultado.append(fila)

        return filas_resultado, errores
    finally:
        os.unlink(tmp_path)


def normalizar_encabezados(df: pd.DataFrame) -> pd.DataFrame | None:
    primera_fila = df.iloc[0].apply(
        lambda x: re.sub(r'\s+', ' ', str(x)).strip().lower()
    )

    nuevas_cols = {}
    for col_idx, valor in primera_fila.items():
        # Limpiar texto del encabezado antes de buscar en aliases
        valor_limpio = valor.split('\n')[0].strip()  # solo primera línea
        nombre_canonico = COLUMN_ALIASES.get(valor_limpio)
        if nombre_canonico:
            nuevas_cols[col_idx] = nombre_canonico

    if not nuevas_cols:
        return None

    df = df[1:].reset_index(drop=True)
    df = df.rename(columns=nuevas_cols)

    # Conservar solo columnas reconocidas
    cols_utiles = [c for c in ["especie", "zona", "periodo",
                               "inicio_directo", "fin_directo"]
                   if c in df.columns]
    return df[cols_utiles] if cols_utiles else None


def procesar_fila(row: pd.Series, errores: list) -> dict | None:
    especie = re.sub(r'\s+', ' ', str(row.get("especie", ""))).strip()

    if not especie or especie.lower() in ("", "nan", "especie", "especies"):
        return None

    zona_raw = re.sub(r'\s+', ' ', str(row.get("zona", ""))).strip().lower()
    zona = ZONAS_VALIDAS.get(zona_raw, zona_raw.upper())

    # ── Caso 1: tablas con inicio/fin separados (camarones) ──────────────
    inicio_dir = re.sub(r'\s+', ' ', str(row.get("inicio_directo", ""))).strip()
    fin_dir    = re.sub(r'\s+', ' ', str(row.get("fin_directo", ""))).strip()

    if inicio_dir and inicio_dir.lower() not in ("nan", ""):
        periodo_i = normalizar_periodo(inicio_dir)
        periodo_f = normalizar_periodo(fin_dir) if fin_dir else None
        if periodo_i:
            return {
                "especie": especie,
                "nombre_cientifico": None,
                "zona": zona,
                "tipo_veda": "FIJA",
                "inicio_veda": periodo_i.inicio,
                "fin_veda": periodo_f.inicio if periodo_f else None,
                "periodo_raw": f"{inicio_dir} / {fin_dir}",
            }

    # ── Caso 2: periodo textual normal ───────────────────────────────────
    periodo_raw = re.sub(r'\s+', ' ', str(row.get("periodo", ""))).strip()

    if not periodo_raw or periodo_raw.lower() == "nan":
        return None

    # ── Caso 3: veda permanente ──────────────────────────────────────────
    if any(kw in periodo_raw.lower() for kw in VEDA_PERMANENTE):
        return {
            "especie": especie,
            "nombre_cientifico": None,
            "zona": zona,
            "tipo_veda": "PERMANENTE",
            "inicio_veda": None,
            "fin_veda": None,
            "periodo_raw": periodo_raw,
        }

    periodo = normalizar_periodo(periodo_raw)
    if not periodo:
        errores.append(f"Periodo no reconocido para '{especie}': '{periodo_raw}'")
        return None

    return {
        "especie": especie,
        "nombre_cientifico": None,
        "zona": zona,
        "tipo_veda": periodo.tipo,
        "inicio_veda": periodo.inicio,
        "fin_veda": periodo.fin,
        "periodo_raw": periodo.raw,
    }