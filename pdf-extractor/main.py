# main.py
import tempfile, os
import camelot
from fastapi import FastAPI, UploadFile, File, HTTPException
from fastapi.responses import JSONResponse
from extractor import extraer_tablas

app = FastAPI(
    title="PDF Extractor — CONAPESCA Vedas",
    description="Microservicio de extracción de tablas de vedas para api-pesca-merida",
    version="1.0.0",
)

@app.get("/health")
def health():
    return {"status": "ok"}

@app.post("/extract")
async def extract(file: UploadFile = File(...)):
    if not file.filename.endswith(".pdf"):
        raise HTTPException(status_code=400, detail="Solo se aceptan archivos PDF")

    contenido = await file.read()
    if len(contenido) == 0:
        raise HTTPException(status_code=400, detail="El archivo está vacío")

    try:
        filas, errores = extraer_tablas(contenido)
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error al procesar PDF: {str(e)}")

    return JSONResponse(content={
        "total": len(filas),
        "errores": len(errores),
        "detalle_errores": errores,  # Útil para ver qué filas fallaron
        "rows": filas,
    })

@app.post("/debug")
async def debug(file: UploadFile = File(...)):
    import traceback
    contenido = await file.read()
    with tempfile.NamedTemporaryFile(suffix=".pdf", delete=False) as tmp:
        tmp.write(contenido)
        tmp_path = tmp.name
    try:
        tables = camelot.read_pdf(tmp_path, pages="all", flavor="lattice")
        resultado = []
        for i, t in enumerate(tables):
            try:
                fila_0 = t.df.iloc[0].tolist()
                fila_1 = t.df.iloc[1].tolist() if len(t.df) > 1 else []
            except Exception as e:
                fila_0 = [f"ERROR: {str(e)}"]
                fila_1 = []
            resultado.append({
                "tabla": i + 1,
                "fila_0": fila_0,
                "fila_1": fila_1,
                "total_filas": len(t.df),
            })
        return resultado
    except Exception:
        return {"error": traceback.format_exc()}
    finally:
        os.unlink(tmp_path)