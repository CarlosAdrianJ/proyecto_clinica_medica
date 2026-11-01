import { useEffect, useState, type FormEvent } from 'react';
import {
  Alert,
  Button,
  Col,
  Container,
  Form,
  Row,
  Spinner,
} from 'react-bootstrap';
import { consultarDisponibilidad } from '../../api/disponibilidad';
import { listarProfesionales } from '../../api/profesionales';
import { crearTurno } from '../../api/turnos';
import type { FranjaDisponible } from '../../types/disponibilidad';
import type { Paciente } from '../../types/paciente';
import type { Profesional } from '../../types/profesional';
import type { TurnoRequest } from '../../types/turno';

const PERFIL_URL = `${import.meta.env?.VITE_API_URL ?? ''}/api/pacientes/mi-perfil`;

interface SolicitarTurnoPageProps {
  token: string;
  onVolver: () => void;
}

function fechaActualLocal(): string {
  const hoy = new Date();
  const anio = hoy.getFullYear();
  const mes = String(hoy.getMonth() + 1).padStart(2, '0');
  const dia = String(hoy.getDate()).padStart(2, '0');

  return `${anio}-${mes}-${dia}`;
}

function mostrarHora(hora: string): string {
  return hora.substring(0, 5);
}

function SolicitarTurnoPage({
  token,
  onVolver,
}: SolicitarTurnoPageProps) {
  const [paciente, setPaciente] = useState<Paciente | null>(null);
  const [profesionales, setProfesionales] = useState<Profesional[]>([]);
  const [franjas, setFranjas] = useState<FranjaDisponible[]>([]);
  const [cargando, setCargando] = useState(true);
  const [cargandoFranjas, setCargandoFranjas] = useState(false);
  const [guardando, setGuardando] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [mensaje, setMensaje] = useState<string | null>(null);

  const [turno, setTurno] = useState<TurnoRequest>({
    pacienteId: 0,
    profesionalId: 0,
    especialidadId: 0,
    fecha: '',
    horaInicio: '',
    observaciones: '',
  });

  useEffect(() => {
    let vigente = true;

    async function cargarDatos() {
      try {
        const [respuestaPerfil, listaProfesionales] = await Promise.all([
          fetch(PERFIL_URL, {
            headers: {
              Authorization: `Bearer ${token}`,
            },
          }),
          listarProfesionales(token),
        ]);

        if (!respuestaPerfil.ok) {
          throw new Error('No se pudo cargar tu ficha de paciente.');
        }

        const perfil = (await respuestaPerfil.json()) as Paciente;

        if (!vigente) return;

        setPaciente(perfil);
        setProfesionales(
          listaProfesionales.filter((profesional) => profesional.activo),
        );
        setTurno((actual) => ({
          ...actual,
          pacienteId: perfil.id,
        }));
      } catch (err) {
        if (vigente) {
          setError(
            err instanceof Error
              ? err.message
              : 'No se pudieron cargar los datos para solicitar el turno.',
          );
        }
      } finally {
        if (vigente) {
          setCargando(false);
        }
      }
    }

    void cargarDatos();

    return () => {
      vigente = false;
    };
  }, [token]);

  const profesionalSeleccionado = profesionales.find(
    (profesional) => profesional.id === turno.profesionalId,
  );

  const especialidades = profesionalSeleccionado
    ? profesionalSeleccionado.especialidades.filter(
        (especialidad) => especialidad.activo,
      )
    : [];

  function cambiarProfesional(profesionalId: number) {
    const profesional = profesionales.find(
      (item) => item.id === profesionalId,
    );

    const especialidadInicial =
      profesional?.especialidades.find((item) => item.activo)?.id ?? 0;

    setTurno((actual) => ({
      ...actual,
      profesionalId,
      especialidadId: especialidadInicial,
      fecha: '',
      horaInicio: '',
    }));
    setFranjas([]);
    setMensaje(null);
    setError(null);
  }

  async function buscarHorarios() {
    setFranjas([]);
    setMensaje(null);
    setError(null);
    setTurno((actual) => ({ ...actual, horaInicio: '' }));

    if (!turno.profesionalId || !turno.fecha) {
      setError('Seleccioná un profesional y una fecha.');
      return;
    }

    setCargandoFranjas(true);

    try {
      const disponibilidad = await consultarDisponibilidad(
        token,
        turno.profesionalId,
        turno.fecha,
      );

      setFranjas(disponibilidad.franjas);

      if (disponibilidad.franjas.length === 0) {
        setMensaje(
          'No hay horarios disponibles para esa fecha. Elegí otra fecha.',
        );
      }
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : 'No se pudo consultar la disponibilidad.',
      );
    } finally {
      setCargandoFranjas(false);
    }
  }

  async function enviarSolicitud(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault();
    setError(null);
    setMensaje(null);

    if (!paciente) {
      setError('No se encontró tu ficha de paciente.');
      return;
    }

    setGuardando(true);

    try {
      await crearTurno(token, {
        ...turno,
        pacienteId: paciente.id,
      });

      setMensaje('El turno se registró correctamente.');
      setFranjas([]);
      setTurno({
        pacienteId: paciente.id,
        profesionalId: 0,
        especialidadId: 0,
        fecha: '',
        horaInicio: '',
        observaciones: '',
      });
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : 'No se pudo registrar el turno.',
      );
    } finally {
      setGuardando(false);
    }
  }

  if (cargando) {
    return (
      <Container className="py-5 text-center">
        <Spinner animation="border" />
        <p className="mt-3">Cargando profesionales...</p>
      </Container>
    );
  }

  return (
    <Container className="py-4">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h1 className="mb-0">Solicitar turno</h1>
        <Button variant="outline-secondary" onClick={onVolver}>
          Volver a mi portal
        </Button>
      </div>

      {error && <Alert variant="danger">{error}</Alert>}
      {mensaje && <Alert variant="success">{mensaje}</Alert>}

      {profesionales.length === 0 ? (
        <Alert variant="info">
          No hay profesionales disponibles para solicitar un turno.
        </Alert>
      ) : (
        <Form onSubmit={enviarSolicitud} className="border rounded p-4">
          <p>
            El turno se asociará a tu ficha de paciente.
          </p>

          <Form.Group className="mb-3">
            <Form.Label>Profesional</Form.Label>
            <Form.Select
              required
              value={turno.profesionalId || ''}
              onChange={(evento) =>
                cambiarProfesional(Number(evento.target.value))
              }
            >
              <option value="">Seleccioná un profesional</option>
              {profesionales.map((profesional) => (
                <option key={profesional.id} value={profesional.id}>
                  {profesional.apellido}, {profesional.nombre}
                </option>
              ))}
            </Form.Select>
          </Form.Group>

          <Form.Group className="mb-3">
            <Form.Label>Especialidad</Form.Label>
            <Form.Select
              required
              value={turno.especialidadId || ''}
              disabled={especialidades.length === 0}
              onChange={(evento) =>
                setTurno((actual) => ({
                  ...actual,
                  especialidadId: Number(evento.target.value),
                }))
              }
            >
              <option value="">Seleccioná una especialidad</option>
              {especialidades.map((especialidad) => (
                <option key={especialidad.id} value={especialidad.id}>
                  {especialidad.nombre}
                </option>
              ))}
            </Form.Select>
          </Form.Group>

          <Row className="align-items-end">
            <Col md={6}>
              <Form.Group className="mb-3">
                <Form.Label>Fecha</Form.Label>
                <Form.Control
                  required
                  type="date"
                  min={fechaActualLocal()}
                  value={turno.fecha}
                  onChange={(evento) => {
                    setTurno((actual) => ({
                      ...actual,
                      fecha: evento.target.value,
                      horaInicio: '',
                    }));
                    setFranjas([]);
                    setMensaje(null);
                  }}
                />
              </Form.Group>
            </Col>

            <Col md={6}>
              <Button
                type="button"
                variant="outline-primary"
                className="w-100 mb-3"
                onClick={() => void buscarHorarios()}
                disabled={cargandoFranjas}
              >
                {cargandoFranjas
                  ? 'Consultando...'
                  : 'Consultar horarios'}
              </Button>
            </Col>
          </Row>

          <Form.Group className="mb-3">
            <Form.Label>Horario disponible</Form.Label>
            <Form.Select
              required
              value={turno.horaInicio}
              disabled={cargandoFranjas || franjas.length === 0}
              onChange={(evento) =>
                setTurno((actual) => ({
                  ...actual,
                  horaInicio: evento.target.value,
                }))
              }
            >
              <option value="">Seleccioná un horario</option>
              {franjas.map((franja) => (
                <option key={franja.horaInicio} value={franja.horaInicio}>
                  {mostrarHora(franja.horaInicio)} a{' '}
                  {mostrarHora(franja.horaFin)}
                </option>
              ))}
            </Form.Select>
          </Form.Group>

          <Form.Group className="mb-4">
            <Form.Label>Observaciones</Form.Label>
            <Form.Control
              as="textarea"
              rows={3}
              maxLength={500}
              value={turno.observaciones}
              onChange={(evento) =>
                setTurno((actual) => ({
                  ...actual,
                  observaciones: evento.target.value,
                }))
              }
            />
          </Form.Group>

          <Button
            type="submit"
            disabled={
              guardando ||
              !paciente ||
              !turno.horaInicio ||
              !turno.especialidadId
            }
          >
            {guardando ? 'Registrando...' : 'Confirmar turno'}
          </Button>
        </Form>
      )}
    </Container>
  );
}

export default SolicitarTurnoPage;