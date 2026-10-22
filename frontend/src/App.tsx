import { useEffect, useState } from 'react';
import { Button, Container, Navbar, Spinner } from 'react-bootstrap';
import { consultarUsuarioActual } from './api/auth';
import {
  eliminarToken,
  guardarToken,
  obtenerToken,
} from './auth/sesion';
import LoginPage from './components/auth/LoginPage';
import TurnosPage from './components/turnos/TurnosPage';
import type {
  LoginResponse,
  UsuarioAutenticado,
} from './types/auth';

function App() {
  const [token, setToken] = useState<string | null>(
    () => obtenerToken(),
  );
  const [usuario, setUsuario] =
    useState<UsuarioAutenticado | null>(null);
  const [cargandoSesion, setCargandoSesion] = useState(
    () => obtenerToken() !== null,
  );

  useEffect(() => {
    const tokenGuardado = obtenerToken();

    if (!tokenGuardado) {
      return;
    }

    let componenteActivo = true;

    consultarUsuarioActual(tokenGuardado)
      .then((usuarioActual) => {
        if (componenteActivo) {
          setUsuario(usuarioActual);
        }
      })
      .catch(() => {
        if (componenteActivo) {
          eliminarToken();
          setToken(null);
        }
      })
      .finally(() => {
        if (componenteActivo) {
          setCargandoSesion(false);
        }
      });

    return () => {
      componenteActivo = false;
    };
  }, []);

  function completarLogin(respuesta: LoginResponse) {
    guardarToken(respuesta.token);
    setToken(respuesta.token);
    setUsuario(respuesta.usuario);
  }

  function cerrarSesion() {
    eliminarToken();
    setToken(null);
    setUsuario(null);
  }

  if (cargandoSesion) {
    return (
      <main className="min-vh-100 d-flex align-items-center justify-content-center">
        <div className="text-center">
          <Spinner animation="border" className="mb-3" />
          <p className="text-secondary mb-0">
            Verificando sesión...
          </p>
        </div>
      </main>
    );
  }

  if (!token || !usuario) {
    return <LoginPage onLogin={completarLogin} />;
  }

  return (
    <>
      <Navbar bg="primary" data-bs-theme="dark">
        <Container fluid>
          <Navbar.Brand>Clínica Médica</Navbar.Brand>

          <div className="d-flex align-items-center gap-3 text-white">
            <div className="text-end">
              <div>{usuario.nombreUsuario}</div>
              <small>{usuario.rol}</small>
            </div>

            <Button
              variant="outline-light"
              size="sm"
              onClick={cerrarSesion}
            >
              Cerrar sesión
            </Button>
          </div>
        </Container>
      </Navbar>

      <TurnosPage token={token} rol={usuario.rol} />
    </>
  );
}

export default App;