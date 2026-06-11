# ORM model definitions for the SmartFit database.
# Each class maps to a MySQL table declared in the RDS instance.
from sqlalchemy import Column, Integer, String, Float, ForeignKey, DateTime, Boolean, Time
from sqlalchemy.orm import relationship
from main import Base
from datetime import datetime

class Usuario(Base):
    """
    Represents a registered user in the SmartFit application.
    Maps to the USUARIO table in the database.
    """
    __tablename__ = "USUARIO"

    id_usuario    = Column(Integer, primary_key=True, index=True)
    nombre        = Column(String(100), nullable=False)
    email         = Column(String(100), unique=True, nullable=False) # Used as login identifier
    password_hash = Column(String(255), nullable=False) # Hashed with bcrypt, never stored in plain text
    horario_entrenamiento = Column(Time, nullable=True) # Used to schedule workout notifications
    fecha_registro = Column(DateTime, default=datetime.utcnow)  # Set automatically on account creation

    # Cascaded relationships: deleting a user removes all their associated data
    rutinas = relationship("Rutina", back_populates="usuario", cascade="all, delete")
    dietas  = relationship("Dieta",  back_populates="usuario", cascade="all, delete")
    medidas = relationship("MedidaCorporal", back_populates="usuario", cascade="all, delete")

class Rutina(Base):
    """
    Represents a workout routine created by a user.
    Maps to the RUTINA table.
    """
    __tablename__ = "RUTINA"

    id_rutina   = Column(Integer, primary_key=True, index=True)
    nombre      = Column(String(100), nullable=False)
    id_usuario  = Column(Integer, ForeignKey("USUARIO.id_usuario", ondelete="CASCADE"))

    usuario    = relationship("Usuario", back_populates="rutinas")
    ejercicios = relationship("RutinaEjercicio", back_populates="rutina", cascade="all, delete")

class Ejercicio(Base):
    """
    Represents an exercise available in the app catalog.
    Exercises are shared across users and assigned to routines via RutinaEjercicio.
    Maps to the EJERCICIO table.
    """
    __tablename__ = "EJERCICIO"

    id_ejercicio      = Column(Integer, primary_key=True, index=True)
    nombre            = Column(String(255), nullable=False)
    grupo_muscular    = Column(String(100))
    equipamiento      = Column(String(100))
    descripcion       = Column(String(250))
    imagen            = Column(String(255))

class RutinaEjercicio(Base):
    """
    Junction table linking routines and exercises (many-to-many).
    Also stores exercise-specific config: sets, reps and display order.
    Maps to the RUTINA_EJERCICIO table.
    """
    __tablename__ = "RUTINA_EJERCICIO"

    id_rutina    = Column(Integer, ForeignKey("RUTINA.id_rutina",       ondelete="CASCADE"), primary_key=True)
    id_ejercicio = Column(Integer, ForeignKey("EJERCICIO.id_ejercicio", ondelete="CASCADE"), primary_key=True)
    series       = Column(Integer, default=3)
    repeticiones = Column(Integer, default=10)
    orden        = Column(Integer)

    rutina    = relationship("Rutina",    back_populates="ejercicios")
    ejercicio = relationship("Ejercicio")

class Dieta(Base):
    """
    Represents a diet plan available in the app catalog.
    Meals are shared across users and assigned to routines via DietaComida.
    Maps to the DIETA table.
    """
    __tablename__ = "DIETA"

    id_dieta          = Column(Integer, primary_key=True, index=True)
    nombre            = Column(String(100), nullable=False)
    objetivo_calorico = Column(Integer, nullable=False)
    proteinas_g       = Column(Float)
    carbohidratos_g   = Column(Float)
    grasas_g          = Column(Float)
    fecha_inicio      = Column(DateTime, nullable=False)
    fecha_fin         = Column(DateTime)
    activo            = Column(Boolean, default=False)
    id_usuario        = Column(Integer, ForeignKey("USUARIO.id_usuario", ondelete="CASCADE"))

    usuario = relationship("Usuario", back_populates="dietas")
    comidas_rel = relationship("DietaComida", back_populates="dieta", cascade="all, delete")

class Comida(Base):
    """
    Represents a meal available in the app catalog.
    Meals are shared across users and assigned to diet plans via DietaComida.
    Maps to the COMIDA table.
    """
    __tablename__ = "COMIDA"

    id_comida          = Column(Integer, primary_key=True, index=True)
    nombre             = Column(String(100), nullable=False)
    calorias_100g      = Column(Integer, nullable=False)
    proteinas_100g     = Column(Integer, nullable=False)
    carbohidratos_100g = Column(Integer, nullable=False)
    grasas_100g        = Column(Integer, nullable=False)
    # QUITAMOS 'dia' e 'id_dieta' de aquí
    id_usuario         = Column(Integer, ForeignKey("USUARIO.id_usuario", ondelete="CASCADE"), nullable=True)
    imagen             = Column(String(255))

    dietas_rel = relationship("DietaComida", back_populates="comida")

class DietaComida(Base):
    """
    Junction table linking diet plans and meals (many-to-many).
    Also stores meal-specific config: Macros and display order.
    Maps to the DIETA_COMIDA table.
    """
    __tablename__ = "DIETA_COMIDA"
    id         = Column(Integer, primary_key=True, index=True)
    id_dieta   = Column(Integer, ForeignKey("DIETA.id_dieta", ondelete="CASCADE"))
    id_comida  = Column(Integer, ForeignKey("COMIDA.id_comida", ondelete="CASCADE"))
    tipo       = Column(String(50), nullable=False)
    dia        = Column(String(20), nullable=False)

    dieta  = relationship("Dieta", back_populates="comidas_rel")
    comida = relationship("Comida", back_populates="dietas_rel")

class MedidaCorporal(Base):
    """
    Represents a body measurement available in the app catalog.
    Measurements are shared across users and assigned to diet plans via DietaComida.
    Maps to the MEDIDA_CORPORAL table.
    """
    __tablename__ = "MEDIDA_CORPORAL"

    id_medida          = Column(Integer, primary_key=True, index=True)
    id_usuario         = Column(Integer, ForeignKey("USUARIO.id_usuario", ondelete="CASCADE"))
    fecha              = Column(DateTime, nullable=False, default=datetime.utcnow) 
    peso_kg            = Column(Float, nullable=False)
    altura_cm          = Column(Float)
    pecho_cm           = Column(Float)
    pierna_cm          = Column(Float)
    brazo_cm           = Column(Float)
    grasa_corporal_pct = Column(Float)
    edad               = Column(Integer, nullable=True)
    sexo               = Column(String(50), nullable=True)

    usuario = relationship("Usuario", back_populates="medidas")

class HistorialEntrenamiento(Base):
    """
    Represents a training session available in the app catalog.
    Sessions are shared across users and assigned to routines via RutinaEjercicio.
    Maps to the HISTORIAL_ENTRENAMIENTO table.
    """
    __tablename__ = "HISTORIAL_ENTRENAMIENTO"

    id_registro      = Column(Integer, primary_key=True, index=True)
    id_usuario       = Column(Integer, ForeignKey("USUARIO.id_usuario",   ondelete="CASCADE"))
    id_ejercicio     = Column(Integer, ForeignKey("EJERCICIO.id_ejercicio", ondelete="CASCADE"))
    id_rutina        = Column(Integer, ForeignKey("RUTINA.id_rutina", ondelete="CASCADE"), nullable=False)
    peso_kg          = Column(Float,   nullable=False)
    repeticiones     = Column(Integer, nullable=False)
    series           = Column(Integer, nullable=False)
    duracion_minutos = Column(Integer, nullable=False, default=0)
    fecha            = Column(DateTime, default=datetime.utcnow)