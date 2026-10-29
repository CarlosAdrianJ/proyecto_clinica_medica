import {
  useCallback,
  useEffect,
  useState,
  type FormEvent,
} from 'react';
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
import { consultarDisponibilidad } from '../../api/disponibilidad';
import {
  cancelarTurno,
  cambiarEstadoTurno,
  consultarMiAgenda,
  crearTurno,
  listarTurnos,
  reprogramarTurno,
} from '../../api/turnos';
import type { FranjaDisponible } from '../../types/disponibilidad';
import type {
  EstadoTurno,
  FiltrosTurno,
  ReprogramarTurnoRequest,
  Turno,
  TurnoRequest,
} from '../../types/turno';

type RolUsuario = 'ADMINISTRADOR' | 'RECEPCIONISTA' | 'PROFESIONAL';

interface TurnosPageProps {
  token: string;
  rol: RolUsuario;
}

const TURNO_VACIO: TurnoRequest = {
  pacienteId: 0,
  profesionalId: 0,
  especialidadId: 0,
  fecha: '',
  horaInicio: '',
  observaciones: '',
};

const REPROGRAMACION_VACIA: ReprogramarTurnoRequest = {
  fecha: '',
  horaInicio: '',
};

function varianteEstado(estado: EstadoTurno): string {
  switch (estado) {
    case 'ATENDIDO':
      return 'success';
    case 'CANCELADO':
      return 'danger';
    case 'AUSENTE':
      return 'secondary';
    default:
      return 'warning';
  }
}

function mostrarHora(hora: string): string {
  return hora.substring(0, 5);
}

function fechaActualLocal(): string {
  const hoy = new Date();
  const anio = hoy.getFullYear();
  const mes = String(hoy.getMonth() + 1).padStart(2, '0');
  const dia = String(hoy.getDate()).padStart(2, '0');

  return `${anio}-${mes}-${dia}`;
}

function esDomingo(fecha: string): boolean {
  if (!fecha) {
    return false;
  }

  const [anio, mes, dia] = fecha.split('-').map(Number);

  return new Date(anio, mes - 1, dia, 12).getDay() === 0;
}

function mensajeSinFranjas(fecha: string): string {
  if (esDomingo(fecha)) {
    return 'La clínica permanece cerrada los domingos.';
  }

  return 'No hay franjas disponibles para esa fecha. La clínica atiende de lunes a viernes de 08:00 a 20:00 y los sábados de 08:00 a 12:00.';
}

function TurnosPage({ token, rol }: TurnosPageProps) {
  const esProfesional = rol === 'PROFESIONAL';

  const [turnos, setTurnos] = useState<Turno[]>([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [pacienteId, setPacienteId] = useState('');
  const [profesionalId, setProfesionalId] = useState('');
  const [fecha, setFecha] = useState('');
  const [estado, setEstado] = useState<EstadoTurno | ''>('');

  const [mostrarNuevo, setMostrarNuevo] = useState(false);
  const [nuevoTurno, setNuevoTurno] =
    useState<TurnoRequest>(TURNO_VACIO);
  const [guardando, setGuardando] = useState(false);
  const [errorFormulario, setErrorFormulario] =
    useState<string | null>(null);

  const [franjasNuevoTurno, setFranjasNuevoTurno] =
    useState<FranjaDisponible[]>([]);
  const [cargandoFranjasNuevo, setCargandoFranjasNuevo] =
    useState(false);
  const [mensajeFranjasNuevo, setMensajeFranjasNuevo] =
    useState<string | null>(null);

  const [turnoReprogramando, setTurnoReprogramando] =
    useState<Turno | null>(null);
  const [reprogramacion, setReprogramacion] =
    useState<ReprogramarTurnoRequest>(REPROGRAMACION_VACIA);

  const [franjasReprogramacion, setFranjasReprogramacion] =
    useState<FranjaDisponible[]>([]);
  const [cargandoFranjasReprogramacion, setCargandoFranjasReprogramacion] =
    useState(false);
  const [mensajeFranjasReprogramacion, setMensajeFranjasReprogramacion] =
    useState<string | null>(null);

  const cargarTurnos = useCallback(async () => {
    if (!token) {
      setError('Debe iniciar sesión para consultar los turnos.');
      setCargando(false);
      return;
    }

    setCargando(true);
    setError(null);

    const filtros: FiltrosTurno = {};

    if (!esProfesional && pacienteId) {
      filtros.pacienteId = Number(pacienteId);
    }

    if (!esProfesional && profesionalId) {
      filtros.profesionalId = Number(profesionalId);
    }

    if (fecha) {
      filtros.fecha = fecha;
    }

    if (estado) {
      filtros.estado = estado;
    }

    try {
      const datos = esProfesional
        ? await consultarMiAgenda(token, {
            fecha: filtros.fecha,
            estado: filtros.estado,
          })
        : await listarTurnos(token, filtros);

      setTurnos(datos);
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : 'No se pudieron cargar los turnos.',
      );
    } finally {
      setCargando(false);
    }
  }, [
    token,
    esProfesional,
    pacienteId,
    profesionalId,
    fecha,
    estado,
  ]);

  useEffect(() => {
    const temporizador = window.setTimeout(() => {
      void cargarTurnos();
    }, 0);

    return () => window.clearTimeout(temporizador);
  }, [cargarTurnos]);

  function limpiarFiltros() {
    setPacienteId('');
    setProfesionalId('');
    setFecha('');
    setEstado('');
  }

  function abrirNuevoTurno() {
    setNuevoTurno(TURNO_VACIO);
    setFranjasNuevoTurno([]);
    setMensajeFranjasNuevo(null);
    setErrorFormulario(null);
    setMostrarNuevo(true);
  }

  async function consultarFranjasNuevoTurno() {
    setFranjasNuevoTurno([]);
    setMensajeFranjasNuevo(null);
    setNuevoTurno((actual) => ({
      ...actual,
      horaInicio: '',
    }));

    if (!nuevoTurno.profesionalId || !nuevoTurno.fecha) {
      setMensajeFranjasNuevo(
        'Ingresá el profesional y la fecha para consultar los horarios.',
      );
      return;
    }

    setCargandoFranjasNuevo(true);

    try {
      const disponibilidad = await consultarDisponibilidad(
        token,
        nuevoTurno.profesionalId,
        nuevoTurno.fecha,
      );

      setFranjasNuevoTurno(disponibilidad.franjas);

      if (disponibilidad.franjas.length === 0) {
        setMensajeFranjasNuevo(mensajeSinFranjas(nuevoTurno.fecha));
      }
    } catch (err) {
      setMensajeFranjasNuevo(
        err instanceof Error
          ? err.message
          : 'No se pudo consultar la disponibilidad.',
      );
    } finally {
      setCargandoFranjasNuevo(false);
    }
  }

  async function guardarNuevoTurno(evento: FormEvent) {
    evento.preventDefault();
    setGuardando(true);
    setErrorFormulario(null);

    try {
      await crearTurno(token, nuevoTurno);
      setMostrarNuevo(false);
      await cargarTurnos();
    } catch (err) {
      setErrorFormulario(
        err instanceof Error
          ? err.message
          : 'No se pudo registrar el turno.',
      );
    } finally {
      setGuardando(false);
    }
  }

  function abrirReprogramacion(turno: Turno) {
    setTurnoReprogramando(turno);
    setReprogramacion(REPROGRAMACION_VACIA);
    setFranjasReprogramacion([]);
    setMensajeFranjasReprogramacion(null);
    setErrorFormulario(null);
  }

  async function consultarFranjasReprogramacion() {
    if (!turnoReprogramando || !reprogramacion.fecha) {
      setFranjasReprogramacion([]);
      setMensajeFranjasReprogramacion(
        'Seleccioná una fecha para consultar los horarios.',
      );
      return;
    }

    setFranjasReprogramacion([]);
    setMensajeFranjasReprogramacion(null);
    setReprogramacion((actual) => ({
      ...actual,
      horaInicio: '',
    }));
    setCargandoFranjasReprogramacion(true);

    try {
      const disponibilidad = await consultarDisponibilidad(
        token,
        turnoReprogramando.profesionalId,
        reprogramacion.fecha,
      );

      setFranjasReprogramacion(disponibilidad.franjas);

      if (disponibilidad.franjas.length === 0) {
        setMensajeFranjasReprogramacion(
          mensajeSinFranjas(reprogramacion.fecha),
        );
      }
    } catch (err) {
      setMensajeFranjasReprogramacion(
        err instanceof Error
          ? err.message
          : 'No se pudo consultar la disponibilidad.',
      );
    } finally {
      setCargandoFranjasReprogramacion(false);
    }
  }

  async function guardarReprogramacion(evento: FormEvent) {
    evento.preventDefault();

    if (!turnoReprogramando) {
      return;
    }

    setGuardando(true);
    setErrorFormulario(null);

    try {
      await reprogramarTurno(
        token,
        turnoReprogramando.id,
        reprogramacion,
      );
      setTurnoReprogramando(null);
      await cargarTurnos();
    } catch (err) {
      setErrorFormulario(
        err instanceof Error
          ? err.message
          : 'No se pudo reprogramar el turno.',
      );
    } finally {
      setGuardando(false);
    }
  }

  async function cancelar(turno: Turno) {
    const confirmar = window.confirm(
      `¿Cancelar el turno de ${turno.pacienteNombre}?`,
    );

    if (!confirmar) {
      return;
    }

    try {
      await cancelarTurno(token, turno.id);
      await cargarTurnos();
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : 'No se pudo cancelar el turno.',
      );
    }
  }

  async function cambiarEstado(
    turno: Turno,
    nuevoEstado: EstadoTurno,
  ) {
    try {
      await cambiarEstadoTurno(token, turno.id, nuevoEstado);
      await cargarTurnos();
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : 'No se pudo cambiar el estado del turno.',
      );
    }
  }

  return (
    <div className="container py-4">
      <div className="d-flex justify-content-between align-items-center mb-3">
        <h1 className="h3 mb-0">
          {esProfesional ? 'Mi agenda' : 'Gestión de turnos'}
        </h1>

        {!esProfesional && (
          <Button onClick={abrirNuevoTurno}>
            Nuevo turno
          </Button>
        )}
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

      <Form className="border rounded p-3 mb-4">
        <Row className="g-3">
          {!esProfesional && (
            <>
              <Col md={3}>
                <Form.Label>Paciente ID</Form.Label>
                <Form.Control
                  type="number"
                  min="1"
                  value={pacienteId}
                  onChange={(evento) =>
                    setPacienteId(evento.target.value)
                  }
                />
              </Col>

              <Col md={3}>
                <Form.Label>Profesional ID</Form.Label>
                <Form.Control
                  type="number"
                  min="1"
                  value={profesionalId}
                  onChange={(evento) =>
                    setProfesionalId(evento.target.value)
                  }
                />
              </Col>
            </>
          )}

          <Col md={3}>
            <Form.Label>Fecha</Form.Label>
            <Form.Control
              type="date"
              value={fecha}
              onChange={(evento) => setFecha(evento.target.value)}
            />
          </Col>

          <Col md={3}>
            <Form.Label>Estado</Form.Label>
            <Form.Select
              value={estado}
              onChange={(evento) =>
                setEstado(evento.target.value as EstadoTurno | '')
              }
            >
              <option value="">Todos</option>
              <option value="PENDIENTE">Pendiente</option>
              <option value="ATENDIDO">Atendido</option>
              <option value="CANCELADO">Cancelado</option>
              <option value="AUSENTE">Ausente</option>
            </Form.Select>
          </Col>

          <Col xs={12}>
            <Button
              variant="outline-secondary"
              onClick={limpiarFiltros}
            >
              Limpiar filtros
            </Button>
          </Col>
        </Row>
      </Form>

      {cargando ? (
        <div className="text-center py-5">
          <Spinner animation="border" />
        </div>
      ) : (
        <Table responsive bordered hover>
          <thead>
            <tr>
              <th>Fecha</th>
              <th>Horario</th>
              <th>Paciente</th>
              <th>Profesional</th>
              <th>Especialidad</th>
              <th>Estado</th>
              <th>Observaciones</th>
              {!esProfesional && <th>Acciones</th>}
            </tr>
          </thead>

          <tbody>
            {turnos.length === 0 ? (
              <tr>
                <td
                  colSpan={esProfesional ? 7 : 8}
                  className="text-center"
                >
                  No hay turnos para mostrar.
                </td>
              </tr>
            ) : (
              turnos.map((turno) => (
                <tr key={turno.id}>
                  <td>{turno.fecha}</td>
                  <td>
                    {mostrarHora(turno.horaInicio)} a{' '}
                    {mostrarHora(turno.horaFin)}
                  </td>
                  <td>{turno.pacienteNombre}</td>
                  <td>{turno.profesionalNombre}</td>
                  <td>{turno.especialidadNombre}</td>
                  <td>
                    <Badge bg={varianteEstado(turno.estado)}>
                      {turno.estado}
                    </Badge>
                  </td>
                  <td>{turno.observaciones ?? '-'}</td>

                  {!esProfesional && (
                    <td>
                      {turno.estado === 'PENDIENTE' ? (
                        <div className="d-flex flex-wrap gap-2">
                          <Button
                            size="sm"
                            variant="outline-primary"
                            onClick={() =>
                              abrirReprogramacion(turno)
                            }
                          >
                            Reprogramar
                          </Button>

                          <Form.Select
                            size="sm"
                            defaultValue=""
                            aria-label="Cambiar estado"
                            onChange={(evento) => {
                              if (evento.target.value) {
                                void cambiarEstado(
                                  turno,
                                  evento.target.value as EstadoTurno,
                                );
                              }
                            }}
                          >
                            <option value="" disabled>
                              Cambiar estado
                            </option>
                            <option value="ATENDIDO">
                              Atendido
                            </option>
                            <option value="AUSENTE">
                              Ausente
                            </option>
                          </Form.Select>

                          <Button
                            size="sm"
                            variant="outline-danger"
                            onClick={() => void cancelar(turno)}
                          >
                            Cancelar
                          </Button>
                        </div>
                      ) : (
                        '-'
                      )}
                    </td>
                  )}
                </tr>
              ))
            )}
          </tbody>
        </Table>
      )}

      <Modal
        show={mostrarNuevo}
        onHide={() => setMostrarNuevo(false)}
      >
        <Form onSubmit={guardarNuevoTurno}>
          <Modal.Header closeButton>
            <Modal.Title>Nuevo turno</Modal.Title>
          </Modal.Header>

          <Modal.Body>
            {errorFormulario && (
              <Alert variant="danger">{errorFormulario}</Alert>
            )}

            <Form.Group className="mb-3">
              <Form.Label>Paciente ID</Form.Label>
              <Form.Control
                required
                type="number"
                min="1"
                value={nuevoTurno.pacienteId || ''}
                onChange={(evento) =>
                  setNuevoTurno({
                    ...nuevoTurno,
                    pacienteId: Number(evento.target.value),
                  })
                }
              />
            </Form.Group>

            <Form.Group className="mb-3">
              <Form.Label>Profesional ID</Form.Label>
              <Form.Control
                required
                type="number"
                min="1"
                value={nuevoTurno.profesionalId || ''}
                onChange={(evento) => {
                  setNuevoTurno({
                    ...nuevoTurno,
                    profesionalId: Number(evento.target.value),
                    horaInicio: '',
                  });
                  setFranjasNuevoTurno([]);
                  setMensajeFranjasNuevo(null);
                }}
              />
            </Form.Group>

            <Form.Group className="mb-3">
              <Form.Label>Especialidad ID</Form.Label>
              <Form.Control
                required
                type="number"
                min="1"
                value={nuevoTurno.especialidadId || ''}
                onChange={(evento) =>
                  setNuevoTurno({
                    ...nuevoTurno,
                    especialidadId: Number(evento.target.value),
                  })
                }
              />
            </Form.Group>

            <Row>
              <Col md={6}>
                <Form.Group className="mb-3">
                  <Form.Label>Fecha</Form.Label>
                  <Form.Control
                    required
                    type="date"
                    min={fechaActualLocal()}
                    value={nuevoTurno.fecha}
                    onChange={(evento) => {
                      setNuevoTurno({
                        ...nuevoTurno,
                        fecha: evento.target.value,
                        horaInicio: '',
                      });
                      setFranjasNuevoTurno([]);
                      setMensajeFranjasNuevo(null);
                    }}
                  />
                </Form.Group>
              </Col>

              <Col md={6} className="d-flex align-items-end mb-3">
                <Button
                  type="button"
                  variant="outline-primary"
                  className="w-100"
                  onClick={() => void consultarFranjasNuevoTurno()}
                  disabled={cargandoFranjasNuevo}
                >
                  {cargandoFranjasNuevo
                    ? 'Consultando...'
                    : 'Consultar horarios'}
                </Button>
              </Col>
            </Row>

            <Form.Group className="mb-3">
              <Form.Label>Horario disponible</Form.Label>
              <Form.Select
                required
                value={nuevoTurno.horaInicio}
                disabled={
                  cargandoFranjasNuevo
                  || franjasNuevoTurno.length === 0
                }
                onChange={(evento) =>
                  setNuevoTurno({
                    ...nuevoTurno,
                    horaInicio: evento.target.value,
                  })
                }
              >
                <option value="">Seleccioná una franja</option>
                {franjasNuevoTurno.map((franja) => (
                  <option
                    key={franja.horaInicio}
                    value={franja.horaInicio}
                  >
                    {mostrarHora(franja.horaInicio)} a{' '}
                    {mostrarHora(franja.horaFin)}
                  </option>
                ))}
              </Form.Select>
            </Form.Group>

            {cargandoFranjasNuevo && (
              <div className="text-center mb-3">
                <Spinner animation="border" size="sm" />
              </div>
            )}

            {mensajeFranjasNuevo && (
              <Alert variant="info">
                {mensajeFranjasNuevo}
              </Alert>
            )}

            <Form.Group>
              <Form.Label>Observaciones</Form.Label>
              <Form.Control
                as="textarea"
                rows={3}
                maxLength={500}
                value={nuevoTurno.observaciones}
                onChange={(evento) =>
                  setNuevoTurno({
                    ...nuevoTurno,
                    observaciones: evento.target.value,
                  })
                }
              />
            </Form.Group>
          </Modal.Body>

          <Modal.Footer>
            <Button
              variant="secondary"
              onClick={() => setMostrarNuevo(false)}
            >
              Cancelar
            </Button>

            <Button
              type="submit"
              disabled={guardando || !nuevoTurno.horaInicio}
            >
              {guardando ? 'Guardando...' : 'Registrar'}
            </Button>
          </Modal.Footer>
        </Form>
      </Modal>

      <Modal
        show={turnoReprogramando !== null}
        onHide={() => setTurnoReprogramando(null)}
      >
        <Form onSubmit={guardarReprogramacion}>
          <Modal.Header closeButton>
            <Modal.Title>Reprogramar turno</Modal.Title>
          </Modal.Header>

          <Modal.Body>
            {errorFormulario && (
              <Alert variant="danger">{errorFormulario}</Alert>
            )}

            <Form.Group className="mb-3">
              <Form.Label>Fecha</Form.Label>
              <Form.Control
                required
                type="date"
                min={fechaActualLocal()}
                value={reprogramacion.fecha}
                onChange={(evento) => {
                  setReprogramacion({
                    ...reprogramacion,
                    fecha: evento.target.value,
                    horaInicio: '',
                  });
                  setFranjasReprogramacion([]);
                  setMensajeFranjasReprogramacion(null);
                }}
              />
            </Form.Group>

            <Button
              type="button"
              variant="outline-primary"
              className="mb-3"
              onClick={() => void consultarFranjasReprogramacion()}
              disabled={cargandoFranjasReprogramacion}
            >
              {cargandoFranjasReprogramacion
                ? 'Consultando...'
                : 'Consultar horarios'}
            </Button>

            <Form.Group className="mb-3">
              <Form.Label>Horario disponible</Form.Label>
              <Form.Select
                required
                value={reprogramacion.horaInicio}
                disabled={
                  cargandoFranjasReprogramacion
                  || franjasReprogramacion.length === 0
                }
                onChange={(evento) =>
                  setReprogramacion({
                    ...reprogramacion,
                    horaInicio: evento.target.value,
                  })
                }
              >
                <option value="">Seleccioná una franja</option>
                {franjasReprogramacion.map((franja) => (
                  <option
                    key={franja.horaInicio}
                    value={franja.horaInicio}
                  >
                    {mostrarHora(franja.horaInicio)} a{' '}
                    {mostrarHora(franja.horaFin)}
                  </option>
                ))}
              </Form.Select>
            </Form.Group>

            {cargandoFranjasReprogramacion && (
              <div className="text-center mb-3">
                <Spinner animation="border" size="sm" />
              </div>
            )}

            {mensajeFranjasReprogramacion && (
              <Alert variant="info">
                {mensajeFranjasReprogramacion}
              </Alert>
            )}
          </Modal.Body>

          <Modal.Footer>
            <Button
              variant="secondary"
              onClick={() => setTurnoReprogramando(null)}
            >
              Cancelar
            </Button>

            <Button
              type="submit"
              disabled={
                guardando || !reprogramacion.horaInicio
              }
            >
              {guardando ? 'Guardando...' : 'Reprogramar'}
            </Button>
          </Modal.Footer>
        </Form>
      </Modal>
    </div>
  );
}

export default TurnosPage;