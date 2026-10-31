
import { useEffect, useState } from 'react';
import {
  Button,
  Container,
  Nav,
  Navbar,
  Spinner,
} from 'react-bootstrap';
import { consultarUsuarioActual } from './api/auth';
import {
  eliminarToken,
  guardarToken,
  obtenerToken,
} from './auth/sesion';
import LoginPage from './components/auth/LoginPage';
import DisponibilidadPage from './components/disponibilidad/DisponibilidadPage';
import EspecialidadesPage from './components/especialidades/EspecialidadesPage';
import HorariosPage from './components/horarios/HorariosPage';
import PacientesPage from './components/pacientes/PacientesPage';
import ProfesionalesPage from './components/profesionales/ProfesionalesPage';
import TurnosPage from './components/turnos/TurnosPage';
import UsuariosPage from './components/usuarios/UsuariosPage';
import type {
  LoginResponse,
  UsuarioAutenticado,
} from './types/auth';

type Pantalla =
  | 'turnos'
  | 'horarios'
  | 'disponibilidad'
  | 'pacientes'
  | 'profesionales'
  | 'especialidades'
  | 'usuarios';

function App() {
  const [token, setToken] = useState<string | null>(
    () => obtenerToken(),
  );
  const [usuario, setUsuario] =
    useState<UsuarioAutenticado | null>(null);
  const [cargandoSesion, setCargandoSesion] = useState(
    () => obtenerToken() !== null,
  );
  const [pantalla, setPantalla] =
    useState<Pantalla>('turnos');

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
    setPantalla('turnos');
  }

  function cerrarSesion() {
    eliminarToken();
    setToken(null);
    setUsuario(null);
    setPantalla('turnos');
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

  const puedeGestionarPacientes =
    usuario.rol === 'ADMINISTRADOR' ||
    usuario.rol === 'RECEPCIONISTA';

  return (
    <>
      <Navbar
        bg="primary"
        data-bs-theme="dark"
        expand="md"
      >
        <Container fluid>
          <Navbar.Brand>Clínica Médica</Navbar.Brand>

          <Navbar.Toggle aria-controls="navegacion-principal" />

          <Navbar.Collapse id="navegacion-principal">
            <Nav className="me-auto">
              {puedeGestionarPacientes && <Nav.Link
                active={pantalla === 'especialidades'}
                onClick={() => setPantalla('especialidades')}
              >
                Especialidades
              </Nav.Link>}
              <Nav.Link
                active={pantalla === 'turnos'}
                onClick={() => setPantalla('turnos')}
              >
                {usuario.rol === 'PROFESIONAL' ? 'Mi agenda' : 'Turnos'}
              </Nav.Link>

              {puedeGestionarPacientes && <Nav.Link
                active={pantalla === 'horarios'}
                onClick={() => setPantalla('horarios')}
              >
                Horarios
              </Nav.Link>}

              {puedeGestionarPacientes && <Nav.Link
                active={pantalla === 'disponibilidad'}
                onClick={() => setPantalla('disponibilidad')}
              >
                Disponibilidad
              </Nav.Link>}

              {puedeGestionarPacientes && (
                <Nav.Link
                  active={pantalla === 'pacientes'}
                  onClick={() => setPantalla('pacientes')}
                >
                  Pacientes
                </Nav.Link>
              )}

              {usuario.rol === 'ADMINISTRADOR' && (
                <Nav.Link
                  active={pantalla === 'usuarios'}
                  onClick={() => setPantalla('usuarios')}
                >
                  Usuarios
                </Nav.Link>
              )}
              {puedeGestionarPacientes && <Nav.Link
                active={pantalla === 'profesionales'}
                onClick={() => setPantalla('profesionales')}
              >
                Profesionales
              </Nav.Link>}
            </Nav>

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
          </Navbar.Collapse>
        </Container>
      </Navbar>

      {usuario.rol === 'PROFESIONAL' ? (
        <TurnosPage token={token} rol={usuario.rol} />
      ) : pantalla === 'especialidades' ? (
        <EspecialidadesPage token={token} rol={usuario.rol} />
      ) : pantalla === 'profesionales' ? (
        <Container fluid className="py-4">
          <ProfesionalesPage token={token} usuario={usuario} />
        </Container>
      ) : pantalla === 'usuarios' &&
      usuario.rol === 'ADMINISTRADOR' ? (
        <UsuariosPage
          token={token}
          usuarioActualId={usuario.id}
        />
      ) : pantalla === 'pacientes' &&
        puedeGestionarPacientes ? (
        <PacientesPage token={token} />
      ) : pantalla === 'disponibilidad' ? (
        <DisponibilidadPage
          token={token}
          rol={usuario.rol}
          profesionalId={usuario.profesionalId}
        />
      ) : pantalla === 'horarios' ? (
        <HorariosPage
          token={token}
          rol={usuario.rol}
          profesionalId={usuario.profesionalId}
        />
      ) : (
        <TurnosPage
          token={token}
          rol={usuario.rol}
        />
      )}
    </>
  );
}

export default App;
