import { useEffect, useState, type FormEvent } from 'react';
import {
  Alert,
  Badge,
  Button,
  Form,
  Modal,
  Spinner,
  Table,
} from 'react-bootstrap';
import {
  actualizarUsuario,
  crearUsuario,
  desactivarUsuario,
  listarProfesionales,
  listarUsuarios,
} from '../../api/usuarios';
import type { Rol } from '../../types/auth';
import type {
  ProfesionalResumen,
  Usuario,
  UsuarioRequest,
} from '../../types/usuario';

interface UsuariosPageProps {
  token: string;
  usuarioActualId: number;
}

const FORMULARIO_VACIO: UsuarioRequest = {
  nombreUsuario: '',
  contrasena: '',
  rol: 'RECEPCIONISTA',
  profesionalId: null,
  activo: true,
};

function UsuariosPage({
  token,
  usuarioActualId,
}: UsuariosPageProps) {
  const [usuarios, setUsuarios] = useState<Usuario[]>([]);
  const [profesionales, setProfesionales] = useState<
    ProfesionalResumen[]
  >([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [mostrarModal, setMostrarModal] = useState(false);
  const [usuarioEditando, setUsuarioEditando] =
    useState<Usuario | null>(null);
  const [formulario, setFormulario] =
    useState<UsuarioRequest>(FORMULARIO_VACIO);
  const [guardando, setGuardando] = useState(false);
  const [errorFormulario, setErrorFormulario] =
    useState<string | null>(null);

  useEffect(() => {
    let componenteActivo = true;

    Promise.all([
      listarUsuarios(token),
      listarProfesionales(token),
    ])
      .then(([usuariosRecibidos, profesionalesRecibidos]) => {
        if (componenteActivo) {
          setUsuarios(usuariosRecibidos);
          setProfesionales(
            profesionalesRecibidos.filter(
              (profesional) => profesional.activo,
            ),
          );
        }
      })
      .catch((err: unknown) => {
        if (componenteActivo) {
          setError(
            err instanceof Error
              ? err.message
              : 'No se pudieron cargar los usuarios.',
          );
        }
      })
      .finally(() => {
        if (componenteActivo) {
          setCargando(false);
        }
      });

    return () => {
      componenteActivo = false;
    };
  }, [token]);

  async function recargarUsuarios() {
    const datos = await listarUsuarios(token);
    setUsuarios(datos);
  }

  function abrirModalCrear() {
    setUsuarioEditando(null);
    setFormulario(FORMULARIO_VACIO);
    setErrorFormulario(null);
    setMostrarModal(true);
  }

  function abrirModalEditar(usuario: Usuario) {
    setUsuarioEditando(usuario);
    setFormulario({
      nombreUsuario: usuario.nombreUsuario,
      contrasena: '',
      rol: usuario.rol,
      profesionalId: usuario.profesionalId,
      activo: usuario.activo,
    });
    setErrorFormulario(null);
    setMostrarModal(true);
  }

  function cerrarModal() {
    if (!guardando) {
      setMostrarModal(false);
    }
  }

  function cambiarRol(rol: Rol) {
    setFormulario((actual) => ({
      ...actual,
      rol,
      profesionalId:
        rol === 'PROFESIONAL' ? actual.profesionalId : null,
    }));
  }

  async function guardar(evento: FormEvent) {
    evento.preventDefault();
    setErrorFormulario(null);

    if (formulario.nombreUsuario.trim().length < 3) {
      setErrorFormulario(
        'El nombre de usuario debe tener al menos 3 caracteres.',
      );
      return;
    }

    if (formulario.contrasena.length < 8) {
      setErrorFormulario(
        usuarioEditando
          ? 'Para actualizar el usuario debe ingresar una contraseña de al menos 8 caracteres.'
          : 'La contraseña debe tener al menos 8 caracteres.',
      );
      return;
    }

    if (
      formulario.rol === 'PROFESIONAL' &&
      formulario.profesionalId === null
    ) {
      setErrorFormulario(
        'Debe seleccionar un profesional para este rol.',
      );
      return;
    }

    setGuardando(true);

    try {
      const datos: UsuarioRequest = {
        ...formulario,
        nombreUsuario: formulario.nombreUsuario.trim(),
        profesionalId:
          formulario.rol === 'PROFESIONAL'
            ? formulario.profesionalId
            : null,
      };

      if (usuarioEditando) {
        await actualizarUsuario(
          token,
          usuarioEditando.id,
          datos,
        );
      } else {
        await crearUsuario(token, datos);
      }

      await recargarUsuarios();
      setMostrarModal(false);
    } catch (err) {
      setErrorFormulario(
        err instanceof Error
          ? err.message
          : 'No se pudo guardar el usuario.',
      );
    } finally {
      setGuardando(false);
    }
  }

  async function confirmarDesactivacion(usuario: Usuario) {
    if (usuario.id === usuarioActualId) {
      setError(
        'No puede desactivar la cuenta con la que inició sesión.',
      );
      return;
    }

    const confirmar = window.confirm(
      `¿Desea desactivar al usuario "${usuario.nombreUsuario}"?`,
    );

    if (!confirmar) {
      return;
    }

    setError(null);

    try {
      await desactivarUsuario(token, usuario.id);
      await recargarUsuarios();
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : 'No se pudo desactivar el usuario.',
      );
    }
  }

  function nombreProfesional(
    profesionalId: number | null,
  ): string {
    if (profesionalId === null) {
      return '—';
    }

    const profesional = profesionales.find(
      (item) => item.id === profesionalId,
    );

    return profesional
      ? `${profesional.apellido}, ${profesional.nombre}`
      : `Profesional #${profesionalId}`;
  }

  if (cargando) {
    return (
      <main className="container py-5 text-center">
        <Spinner animation="border" />
        <p className="text-secondary mt-3">
          Cargando usuarios...
        </p>
      </main>
    );
  }

  return (
    <main className="container py-4">
      <div className="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-4">
        <div>
          <h1 className="h3 mb-1">Usuarios</h1>
          <p className="text-secondary mb-0">
            Administración de cuentas y permisos del sistema.
          </p>
        </div>

        <Button onClick={abrirModalCrear}>
          Nuevo usuario
        </Button>
      </div>

      {error && (
        <Alert
          variant="danger"
          dismissible
          onClose={() => setError(null)}
        >
          {error}
        </Alert>
      )}

      <div className="table-responsive">
        <Table striped bordered hover className="align-middle">
          <thead>
            <tr>
              <th>Usuario</th>
              <th>Rol</th>
              <th>Profesional</th>
              <th>Estado</th>
              <th className="text-end">Acciones</th>
            </tr>
          </thead>

          <tbody>
            {usuarios.length === 0 ? (
              <tr>
                <td colSpan={5} className="text-center py-4">
                  No hay usuarios registrados.
                </td>
              </tr>
            ) : (
              usuarios.map((usuario) => (
                <tr key={usuario.id}>
                  <td>
                    {usuario.nombreUsuario}
                    {usuario.id === usuarioActualId && (
                      <Badge bg="info" className="ms-2">
                        Sesión actual
                      </Badge>
                    )}
                  </td>
                  <td>{usuario.rol}</td>
                  <td>
                    {nombreProfesional(
                      usuario.profesionalId,
                    )}
                  </td>
                  <td>
                    <Badge
                      bg={
                        usuario.activo
                          ? 'success'
                          : 'secondary'
                      }
                    >
                      {usuario.activo
                        ? 'Activo'
                        : 'Inactivo'}
                    </Badge>
                  </td>
                  <td className="text-end">
                    <Button
                      variant="outline-primary"
                      size="sm"
                      className="me-2"
                      onClick={() =>
                        abrirModalEditar(usuario)
                      }
                    >
                      Editar
                    </Button>

                    {usuario.activo && (
                      <Button
                        variant="outline-danger"
                        size="sm"
                        disabled={
                          usuario.id === usuarioActualId
                        }
                        onClick={() =>
                          void confirmarDesactivacion(
                            usuario,
                          )
                        }
                      >
                        Desactivar
                      </Button>
                    )}
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </Table>
      </div>

      <Modal
        show={mostrarModal}
        onHide={cerrarModal}
        backdrop={guardando ? 'static' : true}
        centered
      >
        <Form onSubmit={guardar}>
          <Modal.Header closeButton={!guardando}>
            <Modal.Title>
              {usuarioEditando
                ? 'Editar usuario'
                : 'Nuevo usuario'}
            </Modal.Title>
          </Modal.Header>

          <Modal.Body>
            {errorFormulario && (
              <Alert variant="danger">
                {errorFormulario}
              </Alert>
            )}

            <Form.Group className="mb-3">
              <Form.Label>Nombre de usuario</Form.Label>
              <Form.Control
                required
                minLength={3}
                maxLength={50}
                pattern="[a-zA-Z0-9._-]{3,50}"
                value={formulario.nombreUsuario}
                onChange={(evento) =>
                  setFormulario((actual) => ({
                    ...actual,
                    nombreUsuario: evento.target.value,
                  }))
                }
                disabled={guardando}
              />
              <Form.Text muted>
                Puede utilizar letras, números, punto, guion y
                guion bajo.
              </Form.Text>
            </Form.Group>

            <Form.Group className="mb-3">
              <Form.Label>
                {usuarioEditando
                  ? 'Nueva contraseña'
                  : 'Contraseña'}
              </Form.Label>
              <Form.Control
                required
                type="password"
                minLength={8}
                maxLength={72}
                autoComplete="new-password"
                value={formulario.contrasena}
                onChange={(evento) =>
                  setFormulario((actual) => ({
                    ...actual,
                    contrasena: evento.target.value,
                  }))
                }
                disabled={guardando}
              />
              <Form.Text muted>
                Debe contener entre 8 y 72 caracteres.
                {usuarioEditando &&
                  ' Es obligatoria para guardar la edición.'}
              </Form.Text>
            </Form.Group>

            <Form.Group className="mb-3">
              <Form.Label>Rol</Form.Label>
              <Form.Select
                value={formulario.rol}
                onChange={(evento) =>
                  cambiarRol(evento.target.value as Rol)
                }
                disabled={guardando}
              >
                <option value="ADMINISTRADOR">
                  Administrador
                </option>
                <option value="RECEPCIONISTA">
                  Recepcionista
                </option>
                <option value="PROFESIONAL">
                  Profesional
                </option>
              </Form.Select>
            </Form.Group>

            {formulario.rol === 'PROFESIONAL' && (
              <Form.Group className="mb-3">
                <Form.Label>Profesional asociado</Form.Label>
                <Form.Select
                  required
                  value={formulario.profesionalId ?? ''}
                  onChange={(evento) =>
                    setFormulario((actual) => ({
                      ...actual,
                      profesionalId: evento.target.value
                        ? Number(evento.target.value)
                        : null,
                    }))
                  }
                  disabled={guardando}
                >
                  <option value="">
                    Seleccione un profesional
                  </option>
                  {profesionales.map((profesional) => (
                    <option
                      key={profesional.id}
                      value={profesional.id}
                    >
                      {profesional.apellido},{' '}
                      {profesional.nombre} —{' '}
                      {profesional.matricula}
                    </option>
                  ))}
                </Form.Select>
              </Form.Group>
            )}

            <Form.Check
              type="switch"
              id="usuario-activo"
              label="Usuario activo"
              checked={formulario.activo}
              disabled={
                guardando ||
                usuarioEditando?.id === usuarioActualId
              }
              onChange={(evento) =>
                setFormulario((actual) => ({
                  ...actual,
                  activo: evento.target.checked,
                }))
              }
            />
          </Modal.Body>

          <Modal.Footer>
            <Button
              variant="secondary"
              onClick={cerrarModal}
              disabled={guardando}
            >
              Cancelar
            </Button>

            <Button type="submit" disabled={guardando}>
              {guardando && (
                <Spinner
                  animation="border"
                  size="sm"
                  className="me-2"
                />
              )}
              Guardar
            </Button>
          </Modal.Footer>
        </Form>
      </Modal>
    </main>
  );
}

export default UsuariosPage;