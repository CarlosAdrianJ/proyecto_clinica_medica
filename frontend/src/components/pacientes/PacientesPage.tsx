import { useEffect, useState, type FormEvent } from 'react';
import {
  Alert,
  Badge,
  Button,
  Col,
  Form,
  Modal,
  Row,
  Spinner,
  Table,
} from 'react-bootstrap';
import {
  actualizarPaciente,
  crearPaciente,
  eliminarPaciente,
  listarPacientes,
} from '../../api/pacientes';
import type {
  Paciente,
  PacienteRequest,
} from '../../types/paciente';
import ConfirmacionModal from '../ConfirmacionModal';

interface PacientesPageProps {
  token: string;
}

const FORMULARIO_VACIO: PacienteRequest = {
  nombre: '',
  apellido: '',
  dni: '',
  fechaNacimiento: null,
  telefono: '',
  email: '',
  direccion: '',
};

const FECHA_MAXIMA = new Date(Date.now() - 86_400_000)
  .toISOString()
  .slice(0, 10);

function normalizarTexto(texto: string): string {
  return texto
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLocaleLowerCase()
    .replace(/[^a-z0-9]+/g, ' ')
    .trim();
}

function PacientesPage({ token }: PacientesPageProps) {
  const [pacientes, setPacientes] = useState<Paciente[]>([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [busqueda, setBusqueda] = useState('');

  const [mostrarModal, setMostrarModal] = useState(false);
  const [pacienteEditando, setPacienteEditando] =
    useState<Paciente | null>(null);
  const [formulario, setFormulario] =
    useState<PacienteRequest>(FORMULARIO_VACIO);
  const [guardando, setGuardando] = useState(false);
  const [errorFormulario, setErrorFormulario] =
    useState<string | null>(null);

  const [pacienteAConfirmarBaja, setPacienteAConfirmarBaja] =
    useState<Paciente | null>(null);
  const [dandoDeBaja, setDandoDeBaja] = useState(false);

  useEffect(() => {
    let componenteActivo = true;

    listarPacientes(token)
      .then((datos) => {
        if (componenteActivo) {
          setPacientes(datos);
        }
      })
      .catch((err: unknown) => {
        if (componenteActivo) {
          setError(
            err instanceof Error
              ? err.message
              : 'No se pudieron cargar los pacientes.',
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

  async function recargarPacientes() {
    const datos = await listarPacientes(token);
    setPacientes(datos);
  }

  function abrirModalCrear() {
    setPacienteEditando(null);
    setFormulario(FORMULARIO_VACIO);
    setErrorFormulario(null);
    setMostrarModal(true);
  }

  function abrirModalEditar(paciente: Paciente) {
    setPacienteEditando(paciente);
    setFormulario({
      nombre: paciente.nombre,
      apellido: paciente.apellido,
      dni: paciente.dni,
      fechaNacimiento: paciente.fechaNacimiento,
      telefono: paciente.telefono ?? '',
      email: paciente.email ?? '',
      direccion: paciente.direccion ?? '',
    });
    setErrorFormulario(null);
    setMostrarModal(true);
  }

  function cerrarModal() {
    if (!guardando) {
      setMostrarModal(false);
    }
  }

  async function guardarPaciente(evento: FormEvent) {
    evento.preventDefault();
    setErrorFormulario(null);

    if (
      !formulario.nombre.trim() ||
      !formulario.apellido.trim() ||
      !formulario.dni.trim()
    ) {
      setErrorFormulario(
        'Nombre, apellido y DNI son obligatorios.',
      );
      return;
    }

    if (
      formulario.fechaNacimiento &&
      formulario.fechaNacimiento > FECHA_MAXIMA
    ) {
      setErrorFormulario(
        'La fecha de nacimiento debe ser anterior a hoy.',
      );
      return;
    }

    setGuardando(true);

    try {
      const datos: PacienteRequest = {
        nombre: formulario.nombre.trim(),
        apellido: formulario.apellido.trim(),
        dni: formulario.dni.trim(),
        fechaNacimiento: formulario.fechaNacimiento || null,
        telefono: formulario.telefono.trim(),
        email: formulario.email.trim(),
        direccion: formulario.direccion.trim(),
      };

      if (pacienteEditando) {
        await actualizarPaciente(
          token,
          pacienteEditando.id,
          datos,
        );
      } else {
        await crearPaciente(token, datos);
      }

      await recargarPacientes();
      setMostrarModal(false);
    } catch (err) {
      setErrorFormulario(
        err instanceof Error
          ? err.message
          : 'No se pudo guardar el paciente.',
      );
    } finally {
      setGuardando(false);
    }
  }

  function confirmarBaja(paciente: Paciente) {
    setError(null);
    setPacienteAConfirmarBaja(paciente);
  }

  async function ejecutarBajaPaciente() {
    if (!pacienteAConfirmarBaja || dandoDeBaja) return;

    setDandoDeBaja(true);

    try {
      await eliminarPaciente(token, pacienteAConfirmarBaja.id);
      setPacienteAConfirmarBaja(null);
      await recargarPacientes();
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : 'No se pudo dar de baja al paciente.',
      );
      setPacienteAConfirmarBaja(null);
    } finally {
      setDandoDeBaja(false);
    }
  }

  const termino = normalizarTexto(busqueda);

  const pacientesFiltrados = pacientes.filter((paciente) => {
    if (!termino) {
      return true;
    }

    const datosPaciente = normalizarTexto(
      [
        paciente.nombre,
        paciente.apellido,
        paciente.apellido,
        paciente.nombre,
        paciente.dni,
      ].join(' '),
    );

    return datosPaciente.includes(termino);
  });

  if (cargando) {
    return (
      <main className="container py-5 text-center">
        <Spinner animation="border" />
        <p className="text-secondary mt-3">
          Cargando pacientes...
        </p>
      </main>
    );
  }

  return (
    <main className="container py-4">
      <div className="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-4">
        <div>
          <h1 className="h3 mb-1">Pacientes</h1>
          <p className="text-secondary mb-0">
            Administración de pacientes de la clínica.
          </p>
        </div>

        <Button onClick={abrirModalCrear}>
          Nuevo paciente
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

      <Form.Group className="mb-3">
        <Form.Label>Buscar paciente</Form.Label>
        <Form.Control
          type="search"
          placeholder="Nombre, apellido o DNI"
          value={busqueda}
          onChange={(evento) =>
            setBusqueda(evento.target.value)
          }
        />
      </Form.Group>

      <div className="table-responsive">
        <Table striped bordered hover className="align-middle">
          <thead>
            <tr>
              <th>Paciente</th>
              <th>DNI</th>
              <th>Fecha de nacimiento</th>
              <th>Contacto</th>
              <th>Dirección</th>
              <th>Estado</th>
              <th className="text-end">Acciones</th>
            </tr>
          </thead>

          <tbody>
            {pacientesFiltrados.length === 0 ? (
              <tr>
                <td colSpan={7} className="text-center py-4">
                  No se encontraron pacientes.
                </td>
              </tr>
            ) : (
              pacientesFiltrados.map((paciente) => (
                <tr key={paciente.id}>
                  <td>
                    {paciente.apellido}, {paciente.nombre}
                  </td>
                  <td>{paciente.dni}</td>
                  <td>{paciente.fechaNacimiento ?? '—'}</td>
                  <td>
                    <div>{paciente.telefono || '—'}</div>
                    {paciente.email && (
                      <small className="text-secondary">
                        {paciente.email}
                      </small>
                    )}
                  </td>
                  <td>{paciente.direccion || '—'}</td>
                  <td>
                    <Badge bg="success">Activo</Badge>
                  </td>
                  <td className="text-end">
                    <Button
                      variant="outline-primary"
                      size="sm"
                      className="me-2"
                      onClick={() =>
                        abrirModalEditar(paciente)
                      }
                    >
                      Editar
                    </Button>

                    <Button
                      variant="outline-danger"
                      size="sm"
                      onClick={() => confirmarBaja(paciente)}
                    >
                      Dar de baja
                    </Button>
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
        size="lg"
      >
        <Form onSubmit={guardarPaciente}>
          <Modal.Header closeButton={!guardando}>
            <Modal.Title>
              {pacienteEditando
                ? 'Editar paciente'
                : 'Nuevo paciente'}
            </Modal.Title>
          </Modal.Header>

          <Modal.Body>
            {errorFormulario && (
              <Alert variant="danger">
                {errorFormulario}
              </Alert>
            )}

            <Row>
              <Col md={6}>
                <Form.Group className="mb-3">
                  <Form.Label>Nombre</Form.Label>
                  <Form.Control
                    required
                    maxLength={100}
                    value={formulario.nombre}
                    disabled={guardando}
                    onChange={(evento) =>
                      setFormulario((actual) => ({
                        ...actual,
                        nombre: evento.target.value,
                      }))
                    }
                  />
                </Form.Group>
              </Col>

              <Col md={6}>
                <Form.Group className="mb-3">
                  <Form.Label>Apellido</Form.Label>
                  <Form.Control
                    required
                    maxLength={100}
                    value={formulario.apellido}
                    disabled={guardando}
                    onChange={(evento) =>
                      setFormulario((actual) => ({
                        ...actual,
                        apellido: evento.target.value,
                      }))
                    }
                  />
                </Form.Group>
              </Col>
            </Row>

            <Row>
              <Col md={6}>
                <Form.Group className="mb-3">
                  <Form.Label>DNI</Form.Label>
                  <Form.Control
                    required
                    maxLength={20}
                    value={formulario.dni}
                    disabled={guardando}
                    onChange={(evento) =>
                      setFormulario((actual) => ({
                        ...actual,
                        dni: evento.target.value,
                      }))
                    }
                  />
                </Form.Group>
              </Col>

              <Col md={6}>
                <Form.Group className="mb-3">
                  <Form.Label>
                    Fecha de nacimiento
                  </Form.Label>
                  <Form.Control
                    type="date"
                    max={FECHA_MAXIMA}
                    value={formulario.fechaNacimiento ?? ''}
                    disabled={guardando}
                    onChange={(evento) =>
                      setFormulario((actual) => ({
                        ...actual,
                        fechaNacimiento:
                          evento.target.value || null,
                      }))
                    }
                  />
                </Form.Group>
              </Col>
            </Row>

            <Row>
              <Col md={6}>
                <Form.Group className="mb-3">
                  <Form.Label>Teléfono</Form.Label>
                  <Form.Control
                    maxLength={30}
                    value={formulario.telefono}
                    disabled={guardando}
                    onChange={(evento) =>
                      setFormulario((actual) => ({
                        ...actual,
                        telefono: evento.target.value,
                      }))
                    }
                  />
                </Form.Group>
              </Col>

              <Col md={6}>
                <Form.Group className="mb-3">
                  <Form.Label>
                    Correo electrónico
                  </Form.Label>
                  <Form.Control
                    type="email"
                    maxLength={150}
                    value={formulario.email}
                    disabled={guardando}
                    onChange={(evento) =>
                      setFormulario((actual) => ({
                        ...actual,
                        email: evento.target.value,
                      }))
                    }
                  />
                </Form.Group>
              </Col>
            </Row>

            <Form.Group className="mb-3">
              <Form.Label>Dirección</Form.Label>
              <Form.Control
                maxLength={200}
                value={formulario.direccion}
                disabled={guardando}
                onChange={(evento) =>
                  setFormulario((actual) => ({
                    ...actual,
                    direccion: evento.target.value,
                  }))
                }
              />
            </Form.Group>
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

      <ConfirmacionModal
        mostrar={pacienteAConfirmarBaja !== null}
        titulo="Dar de baja paciente"
        mensaje={
          pacienteAConfirmarBaja
            ? `¿Confirmás la baja de ${pacienteAConfirmarBaja.nombre} ${pacienteAConfirmarBaja.apellido}?`
            : ''
        }
        textoConfirmar="Sí, dar de baja"
        confirmando={dandoDeBaja}
        onCancelar={() => setPacienteAConfirmarBaja(null)}
        onConfirmar={() => void ejecutarBajaPaciente()}
      />
    </main>
  );
}

export default PacientesPage;