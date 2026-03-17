# normalizer.py
import re
from dataclasses import dataclass
from typing import Optional

MESES_ES = {
    "enero": 1, "febrero": 2, "marzo": 3, "abril": 4,
    "mayo": 5, "junio": 6, "julio": 7, "agosto": 8,
    "septiembre": 9, "octubre": 10, "noviembre": 11, "diciembre": 12,
    # Abreviaturas comunes en DOF
    "ene": 1, "feb": 2, "mar": 3, "abr": 4, "may": 5, "jun": 6,
    "jul": 7, "ago": 8, "sep": 9, "oct": 10, "nov": 11, "dic": 12,
}

@dataclass
class PeriodoVeda:
    tipo: str           # "FIJA" | "CICLICA" | "PLURIANUAL"
    inicio: str         # ISO "YYYY-MM-DD" o "MM-DD" según tipo
    fin: str
    raw: str            # texto original, útil para debug

def normalizar_periodo(texto: str) -> Optional[PeriodoVeda]:
    t = texto.strip()
    t = re.sub(r'\s+', ' ', t)       # colapsa \n y espacios múltiples
    t = t.rstrip('.')                  # quita punto final
    t = t.lower()
    t = t.replace(' del ', ' de ')    # "del 2022" → "de 2022"

    # ── Patrón 1: "15 de febrero al 15 de marzo de 2025" (FIJA con año) ──
    p1 = re.search(
        r'(\d{1,2})\s+de\s+(\w+)(?:\s+de\s+(\d{4}))?\s+al?\s+(\d{1,2})\s+de\s+(\w+)(?:\s+de\s+(\d{4}))?',
        t
    )
    if p1:
        dia_i, mes_i_str, anio_i, dia_f, mes_f_str, anio_f = p1.groups()
        mes_i = MESES_ES.get(mes_i_str)
        mes_f = MESES_ES.get(mes_f_str)
        if not mes_i or not mes_f:
            return None

        tiene_anio = bool(anio_i or anio_f)
        if tiene_anio:
            ai = anio_i or anio_f
            af = anio_f or anio_i
            return PeriodoVeda(
                tipo="FIJA",
                inicio=f"{ai}-{mes_i:02d}-{int(dia_i):02d}",
                fin=f"{af}-{mes_f:02d}-{int(dia_f):02d}",
                raw=texto,
            )
        else:
            # Sin año → cíclica anual
            return PeriodoVeda(
                tipo="CICLICA",
                inicio=f"{mes_i:02d}-{int(dia_i):02d}",
                fin=f"{mes_f:02d}-{int(dia_f):02d}",
                raw=texto,
            )

    # ── Patrón 2: "01/mar – 31/jul" (abreviatura con slash) ──
    p2 = re.search(r'(\d{1,2})[/\-](\w+)\s*[–\-al]+\s*(\d{1,2})[/\-](\w+)', t)
    if p2:
        dia_i, mes_i_str, dia_f, mes_f_str = p2.groups()
        mes_i = MESES_ES.get(mes_i_str)
        mes_f = MESES_ES.get(mes_f_str)
        if mes_i and mes_f:
            return PeriodoVeda(
                tipo="CICLICA",
                inicio=f"{mes_i:02d}-{int(dia_i):02d}",
                fin=f"{mes_f:02d}-{int(dia_f):02d}",
                raw=texto,
            )

    # ── Patrón 3: "2025-03-01 a 2025-05-31" (ISO directo) ──
    p3 = re.search(r'(\d{4}-\d{2}-\d{2})\s+a\s+(\d{4}-\d{2}-\d{2})', t)
    if p3:
        return PeriodoVeda(tipo="FIJA", inicio=p3.group(1), fin=p3.group(2), raw=texto)

    # ── Patrón 4: plurianual "2024 al 2026" ──
    p4 = re.search(r'(\d{4})\s+al?\s+(\d{4})', t)
    if p4:
        return PeriodoVeda(
            tipo="PLURIANUAL",
            inicio=f"{p4.group(1)}-01-01",
            fin=f"{p4.group(2)}-12-31",
            raw=texto,
        )

    return None  # No se reconoció el formato — se registrará en el log de error