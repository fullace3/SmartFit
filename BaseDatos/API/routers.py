# API route handlers for the SmartFit application.
# Defines all endpoints and contains the authentication logic (hashing and JWT).
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from jose import jwt
from passlib.context import CryptContext
from datetime import datetime, timedelta, time

from main import get_db, SECRET_KEY
import models, schemas

router = APIRouter() # Called "router" to avoid confusion with the main FastAPI app instance in main.py

# bcrypt is intentionally slow to make brute-force attacks impractical
pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")
ALGORITHM   = "HS256" # Signing algorithm for JWT tokens
TOKEN_HORAS = 24 # Token expires after 24 hours

def hashear(password: str) -> str:
    """Hashes a plain text password using bcrypt."""
    return pwd_context.hash(password)

def verificar(password: str, hashed: str) -> bool:
    """Compares a plain text password against its bcrypt hash. Never decrypts."""
    return pwd_context.verify(password, hashed)

def crear_token(data: dict) -> str:
    """
    Generates a signed JWT token containing the user data and expiration time.
    The token is signed with SECRET_KEY so it cannot be tampered with.
    """
    payload = data.copy()
    payload["exp"] = datetime.utcnow() + timedelta(hours=TOKEN_HORAS)
    return jwt.encode(payload, SECRET_KEY, algorithm=ALGORITHM)


# ══════════════════════════════════════════════
#  USUARIOS
# ══════════════════════════════════════════════

@router.post("/usuarios/registro", response_model=schemas.UsuarioOut, status_code=201, tags=["Usuarios"])
def registrar(datos: schemas.UsuarioCreate, db: Session = Depends(get_db)):
    """
    Registers a new user.
    Returns 400 if the email is already taken.
    The password is hashed before being stored — never saved as plain text.
    """
    if db.query(models.Usuario).filter(models.Usuario.email == datos.email).first():
        raise HTTPException(status_code=400, detail="El email ya está registrado")
    usuario = models.Usuario(
        nombre        = datos.nombre,
        email         = datos.email,
        password_hash = hashear(datos.password) # Plain text is discarded after hashing
    )
    db.add(usuario)
    db.commit()
    db.refresh(usuario)
    return usuario

@router.post("/usuarios/login", response_model=schemas.TokenOut, tags=["Usuarios"])
def login(datos: schemas.LoginSchema, db: Session = Depends(get_db)):
    """
    Authenticates a user and returns a JWT token.
    Returns 401 for both wrong username and wrong password (intentionally vague
    to prevent attackers from knowing which field is incorrect).
    """
    usuario = db.query(models.Usuario).filter(models.Usuario.nombre == datos.nombre).first()
    if not usuario or not verificar(datos.password, usuario.password_hash):
        raise HTTPException(status_code=401, detail="Credenciales incorrectas")
    # Embed user identity in the token — "sub" (subject) is the standard JWT claim for user ID
    token = crear_token({"sub": str(usuario.id_usuario), "email": usuario.email})
    return {"access_token": token, "id_usuario": usuario.id_usuario}

@router.get("/usuarios/{id_usuario}", response_model=schemas.UsuarioOut, tags=["Usuarios"])
def obtener_usuario(id_usuario: int, db: Session = Depends(get_db)):
    """Returns the profile data of a user by ID. Returns 404 if not found."""
    usuario = db.query(models.Usuario).filter(models.Usuario.id_usuario == id_usuario).first()
    if not usuario:
        raise HTTPException(status_code=404, detail="Usuario no encontrado")
    return usuario

@router.put("/usuarios/{id_usuario}", response_model=schemas.UsuarioOut, tags=["Usuarios"])
def actualizar_usuario(id_usuario: int, datos: schemas.UsuarioCreate, db: Session = Depends(get_db)):
    """
    Updates user profile data.
    Password is only re-hashed and updated if a non-empty value is provided,
    allowing the app to update other fields without changing the password.
    """
    usuario = db.query(models.Usuario).filter(models.Usuario.id_usuario == id_usuario).first()
    if not usuario:
        raise HTTPException(status_code=404, detail="Usuario no encontrado")
    
    usuario.nombre = datos.nombre
    usuario.email  = datos.email
    
    if datos.password and datos.password.strip():
        usuario.password_hash = hashear(datos.password)
    
    if datos.horario_entrenamiento:
        # Parse the time string from the app ("HH:MM:SS") into a native Python time object
        # to guarantee data type integrity before saving it to the SQL TIME column
        t = datetime.strptime(datos.horario_entrenamiento, "%H:%M:%S").time()
        usuario.horario_entrenamiento = t
    else:
        usuario.horario_entrenamiento = None
    
    db.commit()
    db.refresh(usuario)
    return usuario

@router.delete("/usuarios/{id_usuario}", status_code=204, tags=["Usuarios"])
def eliminar_usuario(id_usuario: int, db: Session = Depends(get_db)):
    """
    Permanently deletes a user and all their associated data.
    Returns 204 (No Content) on success — no body is returned by convention.
    Cascade deletion handles rutinas, dietas and medidas automatically.
    """
    usuario = db.query(models.Usuario).filter(models.Usuario.id_usuario == id_usuario).first()
    if not usuario:
        raise HTTPException(status_code=404, detail="Usuario no encontrado")
    db.delete(usuario)
    db.commit()


# ══════════════════════════════════════════════
#  MEDIDAS CORPORALES
# ══════════════════════════════════════════════

@router.post("/medidas", response_model=schemas.MedidaOut, status_code=201, tags=["Medidas"])
def crear_medida(datos: schemas.MedidaCreate, db: Session = Depends(get_db)):
    """
    Records a new body measurement for a user.
    Uses model_dump() to unpack all schema fields directly into the ORM model.
    """
    medida = models.MedidaCorporal(**datos.model_dump())
    db.add(medida)
    db.commit()
    db.refresh(medida)
    return medida

@router.get("/medidas/usuario/{id_usuario}", response_model=list[schemas.MedidaOut], tags=["Medidas"])
def historial_medidas(id_usuario: int, db: Session = Depends(get_db)):
    """
    Returns all body measurements for a user sorted by date ascending,
    so the app can plot progress chronologically in the chart.
    """
    return db.query(models.MedidaCorporal)\
             .filter(models.MedidaCorporal.id_usuario == id_usuario)\
             .order_by(models.MedidaCorporal.fecha.asc()).all()

@router.put("/medidas/{id_medida}", response_model=schemas.MedidaOut, tags=["Medidas"])
def editar_medida(id_medida: int, datos: schemas.MedidaCreate, db: Session = Depends(get_db)):
    """Updates an existing body measurement entry."""
    medida = db.query(models.MedidaCorporal).filter(models.MedidaCorporal.id_medida == id_medida).first()
    if not medida:
        raise HTTPException(status_code=404, detail="Medida no encontrada")
    # Dynamically update each field from the schema without listing them one by one
    for campo, valor in datos.model_dump().items():
        setattr(medida, campo, valor)
    db.commit()
    db.refresh(medida)
    return medida

@router.delete("/medidas/{id_medida}", status_code=204, tags=["Medidas"])
def eliminar_medida(id_medida: int, db: Session = Depends(get_db)):
    """Permanently deletes a body measurement. Returns 204 (No Content) on success."""
    medida = db.query(models.MedidaCorporal).filter(models.MedidaCorporal.id_medida == id_medida).first()
    if not medida:
        raise HTTPException(status_code=404, detail="Medida no encontrada")
    db.delete(medida)
    db.commit()


# ══════════════════════════════════════════════
#  EJERCICIOS
# ══════════════════════════════════════════════

@router.post("/ejercicios", response_model=schemas.EjercicioOut, status_code=201, tags=["Ejercicios"])
def crear_ejercicio(datos: schemas.EjercicioCreate, db: Session = Depends(get_db)):
    """Creates a new exercise in the shared catalog."""
    ejercicio = models.Ejercicio(**datos.model_dump())
    db.add(ejercicio)
    db.commit()
    db.refresh(ejercicio)
    return ejercicio

@router.get("/ejercicios", response_model=list[schemas.EjercicioOut], tags=["Ejercicios"])
def todos_ejercicios(db: Session = Depends(get_db)):
    """Returns the full exercise catalog sorted alphabetically by name."""
    return db.query(models.Ejercicio).order_by(models.Ejercicio.nombre).all()

@router.get("/ejercicios/grupo/{grupo}", response_model=list[schemas.EjercicioOut], tags=["Ejercicios"])
def ejercicios_por_grupo(grupo: str, db: Session = Depends(get_db)):
    """Updates an existing exercise in the catalog."""
    return db.query(models.Ejercicio)\
             .filter(models.Ejercicio.grupo_muscular == grupo).all()

@router.put("/ejercicios/{id_ejercicio}", response_model=schemas.EjercicioOut, tags=["Ejercicios"])
def editar_ejercicio(id_ejercicio: int, datos: schemas.EjercicioCreate, db: Session = Depends(get_db)):
    """Updates an existing exercise in the catalog."""
    ejercicio = db.query(models.Ejercicio).filter(models.Ejercicio.id_ejercicio == id_ejercicio).first()
    if not ejercicio:
        raise HTTPException(status_code=404, detail="Ejercicio no encontrado")
    # Dynamically update each field from the schema without listing them one by one
    for campo, valor in datos.model_dump().items():
        setattr(ejercicio, campo, valor)
    db.commit()
    db.refresh(ejercicio)
    return ejercicio

@router.delete("/ejercicios/{id_ejercicio}", status_code=204, tags=["Ejercicios"])
def eliminar_ejercicio(id_ejercicio: int, db: Session = Depends(get_db)):
    """Permanently deletes an exercise from the catalog. Returns 204 (No Content) on success."""
    ejercicio = db.query(models.Ejercicio).filter(models.Ejercicio.id_ejercicio == id_ejercicio).first()
    if not ejercicio:
        raise HTTPException(status_code=404, detail="Ejercicio no encontrado")
    db.delete(ejercicio)
    db.commit()


# ══════════════════════════════════════════════
#  RUTINAS
# ══════════════════════════════════════════════

@router.post("/rutinas", response_model=schemas.RutinaOut, status_code=201, tags=["Rutinas"])
def crear_rutina(datos: schemas.RutinaCreate, db: Session = Depends(get_db)):
    """Creates a new workout routine for a user."""
    rutina = models.Rutina(**datos.model_dump())
    db.add(rutina)
    db.commit()
    db.refresh(rutina)
    return rutina

@router.get("/rutinas/usuario/{id_usuario}", response_model=list[schemas.RutinaOut], tags=["Rutinas"])
def rutinas_usuario(id_usuario: int, db: Session = Depends(get_db)):
    """Returns all routines belonging to a specific user."""
    return db.query(models.Rutina)\
             .filter(models.Rutina.id_usuario == id_usuario).all()

@router.put("/rutinas/{id_rutina}", response_model=schemas.RutinaOut, tags=["Rutinas"])
def editar_rutina(id_rutina: int, datos: schemas.RutinaCreate, db: Session = Depends(get_db)):
    """Updates the name or owner of an existing routine."""
    rutina = db.query(models.Rutina).filter(models.Rutina.id_rutina == id_rutina).first()
    if not rutina:
        raise HTTPException(status_code=404, detail="Rutina no encontrada")
    for campo, valor in datos.model_dump().items():
        setattr(rutina, campo, valor)
    db.commit()
    db.refresh(rutina)
    return rutina

@router.delete("/rutinas/{id_rutina}", status_code=204, tags=["Rutinas"])
def eliminar_rutina(id_rutina: int, db: Session = Depends(get_db)):
    """
    Deletes a routine and all its associated exercises.
    Cascade deletion is handled automatically by the ORM relationship.
    """
    rutina = db.query(models.Rutina).filter(models.Rutina.id_rutina == id_rutina).first()
    if not rutina:
        raise HTTPException(status_code=404, detail="Rutina no encontrada")
    db.delete(rutina)
    db.commit()


# ══════════════════════════════════════════════
#  RUTINA_EJERCICIO  (tabla intermedia)
# ══════════════════════════════════════════════

@router.post("/rutinas/ejercicios", response_model=schemas.RutinaEjercicioOut, status_code=201, tags=["Rutina-Ejercicio"])
def añadir_ejercicio_a_rutina(datos: schemas.RutinaEjercicioCreate, db: Session = Depends(get_db)):
    """
    Adds an exercise to a routine with its sets, reps and display order.
    Uses db.merge() instead of db.add() to handle duplicate entries gracefully —
    if the exercise is already in the routine, it updates it instead of raising an error.
    """
    relacion = models.RutinaEjercicio(**datos.model_dump())
    db.merge(relacion)
    db.commit()
    return relacion

@router.get("/rutinas/{id_rutina}/ejercicios", response_model=list[schemas.RutinaEjercicioOut], tags=["Rutina-Ejercicio"])
def ejercicios_de_rutina(id_rutina: int, db: Session = Depends(get_db)):
    """
    Returns all exercises in a routine sorted by their display order.
    This order is used by the app to render exercises in the correct sequence.
    """
    return db.query(models.RutinaEjercicio)\
             .filter(models.RutinaEjercicio.id_rutina == id_rutina)\
             .order_by(models.RutinaEjercicio.orden).all()

@router.put("/rutinas/{id_rutina}/ejercicios/{id_ejercicio}", response_model=schemas.RutinaEjercicioOut, tags=["Rutina-Ejercicio"])
def editar_ejercicio_de_rutina(id_rutina: int, id_ejercicio: int, datos: schemas.RutinaEjercicioCreate, db: Session = Depends(get_db)):
    """
    Updates sets, reps or order for a specific exercise within a routine.
    Requires both IDs since RutinaEjercicio has a composite primary key.
    """
    relacion = db.query(models.RutinaEjercicio).filter(
        models.RutinaEjercicio.id_rutina   == id_rutina,
        models.RutinaEjercicio.id_ejercicio == id_ejercicio
    ).first()
    if not relacion:
        raise HTTPException(status_code=404, detail="Relación no encontrada")
    for campo, valor in datos.model_dump().items():
        setattr(relacion, campo, valor)
    db.commit()
    db.refresh(relacion)
    return relacion

@router.delete("/rutinas/{id_rutina}/ejercicios/{id_ejercicio}", status_code=204, tags=["Rutina-Ejercicio"])
def quitar_ejercicio_de_rutina(id_rutina: int, id_ejercicio: int, db: Session = Depends(get_db)):
    """Removes an exercise from a routine without deleting the exercise from the catalog."""
    relacion = db.query(models.RutinaEjercicio).filter(
        models.RutinaEjercicio.id_rutina   == id_rutina,
        models.RutinaEjercicio.id_ejercicio == id_ejercicio
    ).first()
    if not relacion:
        raise HTTPException(status_code=404, detail="Relación no encontrada")
    db.delete(relacion)
    db.commit()


# ══════════════════════════════════════════════
#  DIETAS
# ══════════════════════════════════════════════

@router.post("/dietas", response_model=schemas.DietaOut, status_code=201, tags=["Dietas"])
def crear_dieta(datos: schemas.DietaCreate, db: Session = Depends(get_db)):
    """Creates a new diet plan for a user."""
    dieta = models.Dieta(**datos.model_dump())
    db.add(dieta)
    db.commit()
    db.refresh(dieta)
    return dieta

@router.get("/dietas/usuario/{id_usuario}", response_model=list[schemas.DietaOut], tags=["Dietas"])
def dietas_usuario(id_usuario: int, db: Session = Depends(get_db)):
    """Returns all diet plans for a user sorted by start date descending (most recent first)."""
    return db.query(models.Dieta)\
             .filter(models.Dieta.id_usuario == id_usuario)\
             .order_by(models.Dieta.fecha_inicio.desc()).all()

@router.get("/dietas/usuario/{id_usuario}/actual", response_model=schemas.DietaOut, tags=["Dietas"])
def dieta_actual(id_usuario: int, db: Session = Depends(get_db)):
    """Returns the most recently created diet, regardless of whether it is active or not."""
    dieta = db.query(models.Dieta)\
              .filter(models.Dieta.id_usuario == id_usuario)\
              .order_by(models.Dieta.fecha_inicio.desc()).first()
    if not dieta:
        raise HTTPException(status_code=404, detail="No hay ninguna dieta registrada")
    return dieta

@router.get("/dietas/usuario/{id_usuario}/activa", response_model=schemas.DietaOut, tags=["Dietas"])
def dieta_activa(id_usuario: int, db: Session = Depends(get_db)):
    """Returns the diet explicitly marked as active. Only one diet can be active at a time."""
    dieta = db.query(models.Dieta)\
              .filter(models.Dieta.id_usuario == id_usuario, models.Dieta.activo == True).first()
    if not dieta:
        raise HTTPException(status_code=404, detail="No hay ninguna dieta activa")
    return dieta

@router.put("/dietas/{id_dieta}/activar", response_model=schemas.DietaOut, tags=["Dietas"])
def activar_dieta(id_dieta: int, db: Session = Depends(get_db)):
    """
    Sets a diet as active and deactivates all other diets for the same user.
    This ensures only one diet is active at a time — the bulk update runs
    before setting the new one to avoid any race condition.
    """
    dieta = db.query(models.Dieta).filter(models.Dieta.id_dieta == id_dieta).first()
    if not dieta:
        raise HTTPException(status_code=404, detail="Dieta no encontrada")
    db.query(models.Dieta)\
      .filter(models.Dieta.id_usuario == dieta.id_usuario)\
      .update({"activo": False})
    dieta.activo = True
    db.commit()
    db.refresh(dieta)
    return dieta

@router.put("/dietas/{id_dieta}", response_model=schemas.DietaOut, tags=["Dietas"])
def editar_dieta(id_dieta: int, datos: schemas.DietaCreate, db: Session = Depends(get_db)):
    """Updates the nutritional targets and dates of an existing diet."""
    dieta = db.query(models.Dieta).filter(models.Dieta.id_dieta == id_dieta).first()
    if not dieta:
        raise HTTPException(status_code=404, detail="Dieta no encontrada")
    for campo, valor in datos.model_dump().items():
        setattr(dieta, campo, valor)
    db.commit()
    db.refresh(dieta)
    return dieta

@router.delete("/dietas/{id_dieta}", status_code=204, tags=["Dietas"])
def eliminar_dieta(id_dieta: int, db: Session = Depends(get_db)):
    """Deletes a diet and all its associated food entries via cascade."""
    dieta = db.query(models.Dieta).filter(models.Dieta.id_dieta == id_dieta).first()
    if not dieta:
        raise HTTPException(status_code=404, detail="Dieta no encontrada")
    db.delete(dieta)
    db.commit()

# ══════════════════════════════════════════════
#  DIETA_COMIDA (Gestión de relación N:M)
# ══════════════════════════════════════════════

@router.post("/dieta-comida/", response_model=schemas.DietaComidaOut, status_code=201, tags=["Dieta-Comida"])
def añadir_comida_a_dieta(datos: schemas.DietaComidaCreate, db: Session = Depends(get_db)):
    """
    Links a food item to a diet for a specific day and meal type.
    Validates that both the diet and the food exist before creating the relation,
    avoiding orphaned records in the junction table.
    """
    # Verificamos si existe la dieta y la comida
    dieta = db.query(models.Dieta).filter(models.Dieta.id_dieta == datos.id_dieta).first()
    comida = db.query(models.Comida).filter(models.Comida.id_comida == datos.id_comida).first()
    
    if not dieta or not comida:
        raise HTTPException(status_code=404, detail="Dieta o Comida no encontrada")
        
    nueva_relacion = models.DietaComida(**datos.model_dump())
    db.add(nueva_relacion)
    db.commit()
    db.refresh(nueva_relacion)
    return nueva_relacion

@router.get("/dieta-comida/dieta/{id_dieta}", response_model=list[schemas.DietaComidaOut], tags=["Dieta-Comida"])
def obtener_comidas_de_dieta(id_dieta: int, db: Session = Depends(get_db)):
    """Returns all food entries for a diet, including nested food details via the comida relationship."""
    relaciones = db.query(models.DietaComida).filter(models.DietaComida.id_dieta == id_dieta).all()
    return relaciones

@router.delete("/dieta-comida/{id}", status_code=204, tags=["Dieta-Comida"])
def eliminar_comida_de_dieta(id: int, db: Session = Depends(get_db)):
    """Removes a single food entry from a diet without deleting the food from the catalog."""
    relacion = db.query(models.DietaComida).filter(models.DietaComida.id == id).first()
    if not relacion:
        raise HTTPException(status_code=404, detail="Relación no encontrada")
    db.delete(relacion)
    db.commit()

# ══════════════════════════════════════════════
#  COMIDAS
# ══════════════════════════════════════════════

@router.post("/comidas", response_model=schemas.ComidaOut, status_code=201, tags=["Comidas"])
def crear_comida(datos: schemas.ComidaCreate, db: Session = Depends(get_db)):
    """Creates a new food item in the catalog."""
    comida = models.Comida(**datos.model_dump())
    db.add(comida)
    db.commit()
    db.refresh(comida)
    return comida

@router.get("/comidas", response_model=list[schemas.ComidaOut], tags=["Comidas"])
def todas_comidas(db: Session = Depends(get_db)):
    """Returns the full food catalog sorted alphabetically."""
    return db.query(models.Comida).order_by(models.Comida.nombre).all()

@router.get("/comidas/usuario/{id_usuario}", response_model=list[schemas.ComidaOut], tags=["Comidas"])
def comidas_usuario(id_usuario: int, db: Session = Depends(get_db)):
    """Returns only the food items created by a specific user."""
    return db.query(models.Comida)\
             .filter(models.Comida.id_usuario == id_usuario)\
             .order_by(models.Comida.nombre).all()

@router.get("/comidas/usuario/{id_usuario}/buscar", response_model=list[schemas.ComidaOut], tags=["Comidas"])
def buscar_comida(id_usuario: int, nombre: str, db: Session = Depends(get_db)):
    """
    Searches food items by name using a case-insensitive partial match (ilike).
    Used by the app's search bar when the user types in the food list screen.
    """
    return db.query(models.Comida).filter(
        models.Comida.id_usuario == id_usuario,
        models.Comida.nombre.ilike(f"%{nombre}%") # % wildcards allow matching anywhere in the name
    ).all()

@router.get("/comidas/{id_comida}", response_model=schemas.ComidaOut, tags=["Comidas"])
def obtener_comida(id_comida: int, db: Session = Depends(get_db)):
    """Returns the full details of a single food item by ID."""
    comida = db.query(models.Comida).filter(models.Comida.id_comida == id_comida).first()
    if not comida:
        raise HTTPException(status_code=404, detail="Comida no encontrada")
    return comida

@router.put("/comidas/{id_comida}", response_model=schemas.ComidaOut, tags=["Comidas"])
def editar_comida(id_comida: int, datos: schemas.ComidaCreate, db: Session = Depends(get_db)):
    """Updates the nutritional values of an existing food item."""
    comida = db.query(models.Comida).filter(models.Comida.id_comida == id_comida).first()
    if not comida:
        raise HTTPException(status_code=404, detail="Comida no encontrada")
    for campo, valor in datos.model_dump().items():
        setattr(comida, campo, valor)
    db.commit()
    db.refresh(comida)
    return comida

@router.delete("/comidas/{id_comida}", status_code=204, tags=["Comidas"])
def eliminar_comida(id_comida: int, db: Session = Depends(get_db)):
    """Deletes a food item from the catalog. Returns 204 (No Content) on success."""
    comida = db.query(models.Comida).filter(models.Comida.id_comida == id_comida).first()
    if not comida:
        raise HTTPException(status_code=404, detail="Comida no encontrada")
    db.delete(comida)
    db.commit()

# ══════════════════════════════════════════════
#  HISTORIAL DE ENTRENAMIENTO
# ══════════════════════════════════════════════

@router.post("/historial", response_model=schemas.HistorialOut, status_code=201, tags=["Historial"])
def registrar_historial(datos: schemas.HistorialCreate, db: Session = Depends(get_db)):
    """Records a completed exercise entry when the user finishes a workout session."""
    registro = models.HistorialEntrenamiento(**datos.model_dump())
    db.add(registro)
    db.commit()
    db.refresh(registro)
    return registro

@router.get("/progreso/volumen/{id_usuario}", tags=["Progreso"])
def obtener_volumen_progreso(id_usuario: int, db: Session = Depends(get_db)):
    """
    Calculates training volume per session for the progress chart.
    Volume = weight (kg) × reps × sets — a standard metric to measure total workout load.
    Returns a list of date/volume pairs consumed directly by the bar chart in the app.
    """
    # Traemos todos los registros del usuario ordenados por fecha
    logs = db.query(models.HistorialEntrenamiento)\
             .filter(models.HistorialEntrenamiento.id_usuario == id_usuario)\
             .order_by(models.HistorialEntrenamiento.fecha.asc()).all()
    
    progreso = []
    for log in logs:
        volumen = log.peso_kg * log.repeticiones * log.series # Standard volume formula
        progreso.append({
            "fecha": log.fecha.strftime("%Y-%m-%d"),
            "volumen": volumen
        })
    return progreso

@router.get("/historial/usuario/{id_usuario}", tags=["Historial"])
def historial_usuario(id_usuario: int, db: Session = Depends(get_db)):
    """
    Returns the full workout history for a user with exercise names included.
    Uses a JOIN with the Ejercicio table to avoid a separate request per entry.
    Results are sorted by date descending so the app shows the most recent session first.
    The response is built manually (not via a schema) to include the joined exercise name.
    """
    logs = db.query(
        models.HistorialEntrenamiento,
        models.Ejercicio.nombre
    ).join(
        models.Ejercicio,
        models.HistorialEntrenamiento.id_ejercicio == models.Ejercicio.id_ejercicio
    ).filter(
        models.HistorialEntrenamiento.id_usuario == id_usuario
    ).order_by(
        models.HistorialEntrenamiento.fecha.desc()
    ).all()

    return [
        {
            "id_registro":      log.HistorialEntrenamiento.id_registro,
            "id_ejercicio":     log.HistorialEntrenamiento.id_ejercicio,
            "id_rutina":        log.HistorialEntrenamiento.id_rutina,
            "nombre_ejercicio": log.nombre, # Comes from the JOIN, not the history table
            "peso_kg":          log.HistorialEntrenamiento.peso_kg,
            "repeticiones":     log.HistorialEntrenamiento.repeticiones,
            "series":           log.HistorialEntrenamiento.series,
            "duracion_minutos": log.HistorialEntrenamiento.duracion_minutos,
            "fecha":            log.HistorialEntrenamiento.fecha.isoformat()
        }
        for log in logs
    ]