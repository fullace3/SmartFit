# Entry point for the SmartFit FastAPI application.
# Handles database configuration, session management and router registration.
from fastapi import FastAPI
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker, declarative_base
from dotenv import load_dotenv
import os

# Load environment variables from the .env file before accessing them
load_dotenv()
# Read sensitive configuration from environment variables (never hardcoded)
DATABASE_URL = os.getenv("DATABASE_URL") # e.g. mysql+pymysql://user:pass@host/db
SECRET_KEY   = os.getenv("SECRET_KEY") # Used for JWT token signing

# Create the SQLAlchemy engine
# - pool_pre_ping: tests the connection before using it (avoids stale connections)
# - pool_recycle: recycles connections after 30 min to prevent MySQL timeout drops
engine = create_engine(
    DATABASE_URL,
    pool_pre_ping=True,
    pool_recycle=1800
)

# Session factory — autocommit and autoflush are disabled for manual transaction control
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
# Base class for all ORM models (tables are declared as subclasses of Base)
Base         = declarative_base()


def get_db():
    """
    Dependency that provides a database session per request.
    Yields the session and ensures it is closed after the request,
    even if an exception occurs.
    """
    db = SessionLocal()
    try:
        yield db # Inject the session into the route handler
    finally:
        db.close() # Always release the connection back to the pool

# Import the main router after engine/Base are defined to avoid circular imports
from routers import router

# Register all routes defined in the routers module
app = FastAPI(title="SmartFit API")
app.include_router(router)