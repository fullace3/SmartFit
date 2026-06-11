# Pydantic schemas for request validation and response serialization.
from pydantic import BaseModel, EmailStr
from typing import Optional
from datetime import datetime

# ══════════════════════════════════════════════
#  USUARIO
# ══════════════════════════════════════════════

class UsuarioCreate(BaseModel):
    """Data required to register a new user (request body)."""
    nombre: str
    email: EmailStr # Pydantic validates email format automatically
    password: str # Plain text — will be hashed with bcrypt before storing
    horario_entrenamiento: Optional[str] = None # Format: "HH:MM"

class UsuarioOut(BaseModel):
    """User data returned by the API.
    Intentionally excludes password_hash so it never travels over the network,
    even if the ORM object contains it, but returns the user because is needed for the app."""
    id_usuario: int
    nombre: str
    email: str
    horario_entrenamiento: Optional[str] = None
    fecha_registro: datetime

    class Config:
        from_attributes = True # Allows mapping from SQLAlchemy ORM objects directly

class LoginSchema(BaseModel):
    """Credentials required to authenticate an existing user."""
    nombre: str
    password: str # Plain text — compared against the stored bcrypt hash

class TokenOut(BaseModel):
    """
    Schema for the authentication token returned after a successful login.
    Instead of sending credentials on every API request, the client stores 
    this JWT 'bearer' token. Subsequent requests include this token in the 
    Authorization header, allowing the backend to decode it, extract the 
    encrypted user ID, and identify the user securely.
    """
    access_token: str
    token_type: str = "bearer" # Standard OAuth2 token type
    id_usuario: int # Returned so the app can store the session locally

# ══════════════════════════════════════════════
#  MEDIDA CORPORAL
# ══════════════════════════════════════════════

class MedidaCreate(BaseModel):
    """Body measurements recorded by the user to track physical progress."""
    id_usuario: int
    peso_kg: float
    altura_cm: Optional[float] = None
    pecho_cm: Optional[float] = None
    pierna_cm: Optional[float] = None
    brazo_cm: Optional[float] = None
    grasa_corporal_pct: Optional[float] = None
    edad: Optional[int] = None
    sexo: Optional[str] = None

class MedidaOut(MedidaCreate):
    """Extends MedidaCreate adding the generated ID and the recording timestamp."""
    id_medida: int
    fecha: datetime

    class Config:
        from_attributes = True

# ══════════════════════════════════════════════
#  EJERCICIO
# ══════════════════════════════════════════════

class EjercicioCreate(BaseModel):
    nombre: str
    grupo_muscular: Optional[str] = None
    musculo_principal: Optional[str] = None
    equipamiento: Optional[str] = None
    descripcion: Optional[str] = None
    imagen: Optional[str] = None

class EjercicioOut(EjercicioCreate):
    id_ejercicio: int

    class Config:
        from_attributes = True

# ══════════════════════════════════════════════
#  RUTINA
# ══════════════════════════════════════════════

class RutinaCreate(BaseModel):
    nombre: str
    id_usuario: int

class RutinaOut(RutinaCreate):
    id_rutina: int

    class Config:
        from_attributes = True

# ══════════════════════════════════════════════
#  RUTINA_EJERCICIO  (tabla intermedia)
# ══════════════════════════════════════════════

class RutinaEjercicioCreate(BaseModel):
    id_rutina: int
    id_ejercicio: int
    series: int = 3
    repeticiones: int = 10
    orden: int

class RutinaEjercicioOut(RutinaEjercicioCreate):

    class Config:
        from_attributes = True

# ══════════════════════════════════════════════
#  DIETA
# ══════════════════════════════════════════════

class DietaCreate(BaseModel):
    nombre: str
    objetivo_calorico: int
    proteinas_g: float
    carbohidratos_g: float
    grasas_g: float
    fecha_inicio: datetime
    fecha_fin: Optional[datetime] = None
    activo: bool = False
    id_usuario: int

class DietaOut(DietaCreate):
    id_dieta: int
    activo: bool

    class Config:
        from_attributes = True

# ══════════════════════════════════════════════
#  DIETA_COMIDA
# ══════════════════════════════════════════════

class DietaComidaCreate(BaseModel):
    """Links a food item to a diet, specifying the meal type and day of the week."""
    id_dieta: int
    id_comida: int
    tipo: str
    dia: str

class DietaComidaOut(BaseModel):
    id: int
    id_dieta: int
    id_comida: int
    tipo: str
    dia: str
    comida: Optional["ComidaOut"] = None # Para ver los detalles de la comida al listar la dieta

    class Config:
        from_attributes = True

# ══════════════════════════════════════════════
#  COMIDA
# ══════════════════════════════════════════════

class ComidaCreate(BaseModel):
    nombre: str
    calorias_100g: int
    proteinas_100g: int
    carbohidratos_100g: int
    grasas_100g: int
    id_usuario: Optional[int] = None
    imagen: Optional[str] = None

class ComidaOut(ComidaCreate):
    id_comida: int

    class Config:
        from_attributes = True

# ══════════════════════════════════════════════
#  HISTORIAL DE ENTRENAMIENTO
# ══════════════════════════════════════════════

class HistorialCreate(BaseModel):
    """
    Records a completed exercise within a workout session.
    One entry is saved per exercise when the user finishes training.
    """
    id_usuario: int
    id_ejercicio: int
    id_rutina: int
    peso_kg: float
    repeticiones: int
    series: int
    duracion_minutos: int = 0 # Defaults to 0 if the user does not track session duration

class HistorialOut(HistorialCreate):
    id_registro: int
    fecha: datetime

    class Config:
        from_attributes = True