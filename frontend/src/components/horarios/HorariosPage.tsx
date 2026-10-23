
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
  actualizarHorario,
  crearHorario,
  eliminarHorario,
  listarHorarios,
  listarHorariosPorProfesional,
  listarProfesionalesParaHorarios,
} from '../../api/horarios';
import type { Rol } from '../../types/auth';
import type {
  DiaSemana,
  HorarioAtencion,
  HorarioAtencionRequest,
  ProfesionalHorario,
} from '../../types/horario';

interface HorariosPageProps {
  token: string;
  rol: Rol;
  profesionalId: number | null;
}

interface HorarioFormData {
  profesionalId: string;
  diaSemana: DiaSemana;
  horaDesde: string;
  horaHasta: string;
  duracionTurno: string;
}

const DIAS: DiaSemana[] = [
  'LUNES',
  'MARTES',
  'MIERCOLES',
  'JUEVES',
  'VIERNES',
  'SABADO',
  'DOMINGO',
];

const NOMBRES_DIAS: Record<DiaSemana, string> = {
  LUNES: 'Lunes',
  MARTES: 'Martes',
  MIERCOLES: 'Miércoles',
  JUEVES: 'Jueves',
  VIERNES: 'Viernes',
  SABADO: 'Sábado',
  DOMINGO: 'Domingo',
};

const HORAS_CADA_15_MINUTOS = Array.from(
  { length: 96 },
  (_, indice) => {
    const horas = Math.floor(indice / 4)
      .toString()
      .padStart(2, '0');
    const minutos = ((indice % 4) * 15)
      .toString()
      .padStart(2, '0');

    return `${horas}:${minutos}`;
  },
);

const FORMULARIO_VACIO: HorarioFormData = {
  profesionalId: '',
  diaSemana: 'LUNES',
  horaDesde: '',
  horaHasta: '',
  duracionTurno: '30',
};

function mostrarHora(hora: string): string {
  return hora.substring(0, 5);
}

function mensajeError(error: unknown): string {
  return error instanceof Error
    ? error.message
    : 'Ocurrió un error inesperado.';
}

function HorariosPage({
  token,
  rol,
  profesionalId,
}: HorariosPageProps) {
  const esAdministrador = rol === 'ADMINISTRADOR';
  const esProfesional = rol === 'PROFESIONAL';

  const [horarios, setHorarios] = useState<HorarioAtencion[]>([]);
  const [profesionales, setProfesionales] = useState<
    ProfesionalHorario[]
  >([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [filtroProfesional, setFiltroProfesional] = useState('');
  const [filtroDia, setFiltroDia] = useState('');

  const [mostrarModal, setMostrarModal] = useState(false);
  const [horarioEditando, setHorarioEditando] =
    useState<HorarioAtencion | null>(null);
  const [formulario, setFormulario] =
    useState<HorarioFormData>(FORMULARIO_VACIO);
  const [guardando, setGuardando] = useState(false);
  const [errorFormulario, setErrorFormulario] =
    useState<string | null>(null);

  useEffect(() => {
    let componenteActivo = true;

    async function obtenerDatos() {
      try {
        let horariosRecibidos: HorarioAtencion[];

        if (esProfesional) {
          if (profesionalId === null) {
            throw new Error(
              'La cuenta no tiene un profesional asociado.',
            );
          }

          horariosRecibidos = await listarHorariosPorProfesional(
            token,
            profesionalId,
          );
        } else {
          horariosRecibidos = await listarHorarios(token);
        }

        let profesionalesRecibidos: ProfesionalHorario[] = [];

        if (esAdministrador) {
          profesionalesRecibidos =
            await listarProfesionalesParaHorarios(token);
        }

        if (componenteActivo) {
          setHorarios(horariosRecibidos);
          setProfesionales(
            profesionalesRecibidos.filter(
              (profesional) => profesional.activo,
            ),
          );
        }
      } catch (err) {
        if (componenteActivo) {
          setError(mensajeError(err));
        }
      } finally {
        if (componenteActivo) {
          setCargando(false);
        }
      }
    }

    void obtenerDatos();

    return () => {
      componenteActivo = false;
    };
  }, [esAdministrador, esProfesional, profesionalId, token]);

  async function recargarHorarios() {
    setCargando(true);
    setError(null);

    try {
      const datos =
        esProfesional && profesionalId !== null
          ? await listarHorariosPorProfesional(token, profesionalId)
          : await listarHorarios(token);

      setHorarios(datos);
    } catch (err) {
      setError(mensajeError(err));
    } finally {
      setCargando(false);
    }
  }

  function abrirModalCrear() {
    setHorarioEditando(null);
    setFormulario(FORMULARIO_VACIO);
    setErrorFormulario(null);
    setMostrarModal(true);
  }

  function abrirModalEditar(horario: HorarioAtencion) {
    setHorarioEditando(horario);
    setFormulario({
      profesionalId: String(horario.profesionalId),
      diaSemana: horario.diaSemana,
      horaDesde: mostrarHora(horario.horaDesde),
      horaHasta: mostrarHora(horario.horaHasta),
      duracionTurno: String(horario.duracionTurno),
    });
    setErrorFormulario(null);
    setMostrarModal(true);
  }

  function cerrarModal() {
    if (!guardando) {
      setMostrarModal(false);
    }
  }

  function actualizarCampo(
    campo: keyof HorarioFormData,
    valor: string,
  ) {
    setFormulario((actual) => ({
      ...actual,
      [campo]: valor,
    }));
  }

  async function guardarHorario(evento: FormEvent) {
    evento.preventDefault();
    setErrorFormulario(null);

    const profesionalSeleccionado = Number(
      formulario.profesionalId,
    );
    const duracionSeleccionada = Number(
      formulario.duracionTurno,
    );

    if (
      !profesionalSeleccionado ||
      !formulario.horaDesde ||
      !formulario.horaHasta ||
      !duracionSeleccionada
    ) {
      setErrorFormulario(
        'Complete todos los campos obligatorios.',
      );
      return;
    }

    if (formulario.horaDesde >= formulario.horaHasta) {
      setErrorFormulario(
        'La hora de inicio debe ser anterior a la hora final.',
      );
      return;
    }

    const datos: HorarioAtencionRequest = {
      profesionalId: profesionalSeleccionado,
      diaSemana: formulario.diaSemana,
      horaDesde: formulario.horaDesde,
      horaHasta: formulario.horaHasta,
      duracionTurno: duracionSeleccionada,
    };

    setGuardando(true);

    try {
      if (horarioEditando) {
        await actualizarHorario(
          token,
          horarioEditando.id,
          datos,
        );
      } else {
        await crearHorario(token, datos);
      }

      setMostrarModal(false);
      await recargarHorarios();
    } catch (err) {
      setErrorFormulario(mensajeError(err));
    } finally {
      setGuardando(false);
    }
  }

  async function darDeBaja(horario: HorarioAtencion) {
    const profesional =
      `${horario.profesionalNombre} ${horario.profesionalApellido}`;
    const confirmar = window.confirm(
      `¿Dar de baja el horario de ${profesional} del día ${NOMBRES_DIAS[horario.diaSemana]}?`,
    );

    if (!confirmar) {
      return;
    }

    setError(null);

    try {
      await eliminarHorario(token, horario.id);
      await recargarHorarios();
    } catch (err) {
      setError(mensajeError(err));
    }
  }

  function limpiarFiltros() {
    setFiltroProfesional('');
    setFiltroDia('');
  }

  const horariosFiltrados = horarios.filter((horario) => {
    const nombreProfesional =
      `${horario.profesionalNombre} ${horario.profesionalApellido}`
        .toLocaleLowerCase();

    const coincideProfesional = nombreProfesional.includes(
      filtroProfesional.trim().toLocaleLowerCase(),
    );

    const coincideDia =
      !filtroDia || horario.diaSemana === filtroDia;

    return coincideProfesional && coincideDia;
  });

  if (cargando) {
    return (
      <main className="container py-5 text-center">
        <Spinner animation="border" />
        <p className="text-secondary mt-3">
          Cargando horarios de atención...
        </p>
      </main>
    );
  }

  return (
    <main className="container py-4">
      <div className="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-4">
        <div>
          <h1 className="h3 mb-1">
            {esProfesional
              ? 'Mis horarios de atención'
              : 'Horarios de atención'}
          </h1>

          <p className="text-secondary mb-0">
            {esAdministrador
              ? 'Administración de días, franjas horarias y duración de los turnos.'
              : 'Consulta de días y franjas horarias de atención.'}
          </p>
        </div>

        {esAdministrador && (
          <Button onClick={abrirModalCrear}>
            Nuevo horario
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

      {!esProfesional && (
        <section className="border rounded p-3 mb-4 bg-light">
          <Row className="g-3 align-items-end">
            <Col md={5}>
              <Form.Group controlId="filtroProfesionalHorario">
                <Form.Label>Profesional</Form.Label>
                <Form.Control
                  value={filtroProfesional}
                  onChange={(evento) =>
                    setFiltroProfesional(evento.target.value)
                  }
                  placeholder="Buscar por nombre o apellido"
                />
              </Form.Group>
            </Col>

            <Col md={4}>
              <Form.Group controlId="filtroDiaHorario">
                <Form.Label>Día</Form.Label>
                <Form.Select
                  value={filtroDia}
                  onChange={(evento) =>
                    setFiltroDia(evento.target.value)
                  }
                >
                  <option value="">Todos los días</option>

                  {DIAS.map((dia) => (
                    <option key={dia} value={dia}>
                      {NOMBRES_DIAS[dia]}
                    </option>
                  ))}
                </Form.Select>
              </Form.Group>
            </Col>

            <Col md={3}>
              <Button
                variant="outline-secondary"
                className="w-100"
                onClick={limpiarFiltros}
              >
                Limpiar filtros
              </Button>
            </Col>
          </Row>
        </section>
      )}

      {horariosFiltrados.length === 0 ? (
        <Alert variant="info">
          No se encontraron horarios de atención.
        </Alert>
      ) : (
        <div className="table-responsive">
          <Table striped bordered hover className="align-middle">
            <thead>
              <tr>
                {!esProfesional && <th>Profesional</th>}
                <th>Día</th>
                <th>Desde</th>
                <th>Hasta</th>
                <th>Duración</th>
                <th>Estado</th>
                {esAdministrador && <th>Acciones</th>}
              </tr>
            </thead>

            <tbody>
              {horariosFiltrados.map((horario) => (
                <tr key={horario.id}>
                  {!esProfesional && (
                    <td>
                      {horario.profesionalNombre}{' '}
                      {horario.profesionalApellido}
                    </td>
                  )}

                  <td>{NOMBRES_DIAS[horario.diaSemana]}</td>
                  <td>{mostrarHora(horario.horaDesde)}</td>
                  <td>{mostrarHora(horario.horaHasta)}</td>
                  <td>{horario.duracionTurno} minutos</td>

                  <td>
                    <Badge
                      bg={
                        horario.activo
                          ? 'success'
                          : 'secondary'
                      }
                    >
                      {horario.activo ? 'Activo' : 'Inactivo'}
                    </Badge>
                  </td>

                  {esAdministrador && (
                    <td>
                      <div className="d-flex gap-2">
                        <Button
                          size="sm"
                          variant="outline-primary"
                          onClick={() =>
                            abrirModalEditar(horario)
                          }
                        >
                          Editar
                        </Button>

                        <Button
                          size="sm"
                          variant="outline-danger"
                          onClick={() =>
                            void darDeBaja(horario)
                          }
                        >
                          Dar de baja
                        </Button>
                      </div>
                    </td>
                  )}
                </tr>
              ))}
            </tbody>
          </Table>
        </div>
      )}

      <Modal
        show={mostrarModal}
        onHide={cerrarModal}
        centered
      >
        <Form onSubmit={guardarHorario}>
          <Modal.Header closeButton>
            <Modal.Title>
              {horarioEditando
                ? 'Editar horario'
                : 'Nuevo horario'}
            </Modal.Title>
          </Modal.Header>

          <Modal.Body>
            {errorFormulario && (
              <Alert variant="danger">
                {errorFormulario}
              </Alert>
            )}

            <Form.Group
              className="mb-3"
              controlId="profesionalHorario"
            >
              <Form.Label>Profesional</Form.Label>

              <Form.Select
                required
                value={formulario.profesionalId}
                onChange={(evento) =>
                  actualizarCampo(
                    'profesionalId',
                    evento.target.value,
                  )
                }
              >
                <option value="">
                  Seleccione un profesional
                </option>

                {profesionales.map((profesional) => (
                  <option
                    key={profesional.id}
                    value={profesional.id}
                  >
                    {profesional.apellido}, {profesional.nombre}
                    {' - '}
                    {profesional.matricula}
                  </option>
                ))}
              </Form.Select>
            </Form.Group>

            <Form.Group
              className="mb-3"
              controlId="diaHorario"
            >
              <Form.Label>Día de atención</Form.Label>

              <Form.Select
                required
                value={formulario.diaSemana}
                onChange={(evento) =>
                  actualizarCampo(
                    'diaSemana',
                    evento.target.value as DiaSemana,
                  )
                }
              >
                {DIAS.map((dia) => (
                  <option key={dia} value={dia}>
                    {NOMBRES_DIAS[dia]}
                  </option>
                ))}
              </Form.Select>
            </Form.Group>

            <Row>
              <Col sm={6}>
                <Form.Group
                  className="mb-3"
                  controlId="horaDesdeHorario"
                >
                  <Form.Label>Hora desde</Form.Label>

                  <Form.Select
                    required
                    value={formulario.horaDesde}
                    onChange={(evento) =>
                      actualizarCampo(
                        'horaDesde',
                        evento.target.value,
                      )
                    }
                  >
                    <option value="">
                      Seleccione una hora
                    </option>

                    {HORAS_CADA_15_MINUTOS.map((hora) => (
                      <option key={hora} value={hora}>
                        {hora}
                      </option>
                    ))}
                  </Form.Select>
                </Form.Group>
              </Col>

              <Col sm={6}>
                <Form.Group
                  className="mb-3"
                  controlId="horaHastaHorario"
                >
                  <Form.Label>Hora hasta</Form.Label>

                  <Form.Select
                    required
                    value={formulario.horaHasta}
                    onChange={(evento) =>
                      actualizarCampo(
                        'horaHasta',
                        evento.target.value,
                      )
                    }
                  >
                    <option value="">
                      Seleccione una hora
                    </option>

                    {HORAS_CADA_15_MINUTOS.map((hora) => (
                      <option key={hora} value={hora}>
                        {hora}
                      </option>
                    ))}
                  </Form.Select>
                </Form.Group>
              </Col>
            </Row>

            <Form.Group controlId="duracionHorario">
              <Form.Label>
                Duración de cada turno en minutos
              </Form.Label>

              <Form.Control
                required
                type="number"
                min={1}
                value={formulario.duracionTurno}
                onChange={(evento) =>
                  actualizarCampo(
                    'duracionTurno',
                    evento.target.value,
                  )
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
              {guardando ? (
                <>
                  <Spinner
                    animation="border"
                    size="sm"
                    className="me-2"
                  />
                  Guardando...
                </>
              ) : (
                'Guardar'
              )}
            </Button>
          </Modal.Footer>
        </Form>
      </Modal>
    </main>
  );
}

export default HorariosPage;