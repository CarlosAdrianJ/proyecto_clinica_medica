import {
  useEffect,
  useState,
  type FormEvent,
} from 'react';
import {
  Alert,
  Button,
  Card,
  Col,
  Form,
  Row,
  Spinner,
  Table,
} from 'react-bootstrap';
import { consultarDisponibilidad } from '../../api/disponibilidad';
import { listarProfesionalesParaHorarios } from '../../api/horarios';
import type { Rol } from '../../types/auth';
import type { Disponibilidad } from '../../types/disponibilidad';
import type { ProfesionalHorario } from '../../types/horario';

interface DisponibilidadPageProps {
  token: string;
  rol: Rol;
  profesionalId: number | null;
}

function obtenerFechaActual(): string {
  const fecha = new Date();
  const anio = fecha.getFullYear();
  const mes = String(fecha.getMonth() + 1).padStart(2, '0');
  const dia = String(fecha.getDate()).padStart(2, '0');

  return `${anio}-${mes}-${dia}`;
}

function mostrarHora(hora: string): string {
  return hora.substring(0, 5);
}

function mostrarFecha(fecha: string): string {
  return new Intl.DateTimeFormat('es-AR', {
    dateStyle: 'long',
    timeZone: 'UTC',
  }).format(new Date(`${fecha}T00:00:00Z`));
}

function mensajeError(error: unknown): string {
  return error instanceof Error
    ? error.message
    : 'Ocurrió un error inesperado.';
}

function DisponibilidadPage({
  token,
  rol,
  profesionalId,
}: DisponibilidadPageProps) {
  const esProfesional = rol === 'PROFESIONAL';

  const [profesionales, setProfesionales] = useState<
    ProfesionalHorario[]
  >([]);
  const [profesionalSeleccionado, setProfesionalSeleccionado] =
    useState(() =>
      profesionalId === null ? '' : String(profesionalId),
    );
  const [fecha, setFecha] = useState(obtenerFechaActual);
  const [resultado, setResultado] =
    useState<Disponibilidad | null>(null);
  const [cargandoProfesionales, setCargandoProfesionales] =
    useState(!esProfesional);
  const [consultando, setConsultando] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (esProfesional) {
      return;
    }

    let componenteActivo = true;

    listarProfesionalesParaHorarios(token)
      .then((datos) => {
        if (componenteActivo) {
          setProfesionales(
            datos.filter((profesional) => profesional.activo),
          );
        }
      })
      .catch((err) => {
        if (componenteActivo) {
          setError(mensajeError(err));
        }
      })
      .finally(() => {
        if (componenteActivo) {
          setCargandoProfesionales(false);
        }
      });

    return () => {
      componenteActivo = false;
    };
  }, [esProfesional, token]);

  async function consultar(evento: FormEvent) {
    evento.preventDefault();
    setError(null);
    setResultado(null);

    const idProfesional = Number(profesionalSeleccionado);

    if (!idProfesional || !fecha) {
      setError('Seleccione un profesional y una fecha.');
      return;
    }

    setConsultando(true);

    try {
      const datos = await consultarDisponibilidad(
        token,
        idProfesional,
        fecha,
      );

      setResultado(datos);
    } catch (err) {
      setError(mensajeError(err));
    } finally {
      setConsultando(false);
    }
  }

  const profesionalActual = profesionales.find(
    (profesional) =>
      profesional.id === Number(profesionalSeleccionado),
  );

  return (
    <main className="container py-4">
      <div className="mb-4">
        <h1 className="h3 mb-1">
          {esProfesional
            ? 'Mi disponibilidad'
            : 'Consulta de disponibilidad'}
        </h1>

        <p className="text-secondary mb-0">
          Consulte las franjas libres de atención para una fecha
          determinada.
        </p>
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

      <Card className="mb-4">
        <Card.Body>
          <Form onSubmit={consultar}>
            <Row className="g-3 align-items-end">
              {!esProfesional && (
                <Col md={5}>
                  <Form.Group controlId="profesionalDisponibilidad">
                    <Form.Label>Profesional</Form.Label>

                    <Form.Select
                      required
                      value={profesionalSeleccionado}
                      disabled={cargandoProfesionales}
                      onChange={(evento) => {
                        setProfesionalSeleccionado(
                          evento.target.value,
                        );
                        setResultado(null);
                      }}
                    >
                      <option value="">
                        {cargandoProfesionales
                          ? 'Cargando profesionales...'
                          : 'Seleccione un profesional'}
                      </option>

                      {profesionales.map((profesional) => (
                        <option
                          key={profesional.id}
                          value={profesional.id}
                        >
                          {profesional.apellido},{' '}
                          {profesional.nombre} -{' '}
                          {profesional.matricula}
                        </option>
                      ))}
                    </Form.Select>
                  </Form.Group>
                </Col>
              )}

              <Col md={esProfesional ? 6 : 4}>
                <Form.Group controlId="fechaDisponibilidad">
                  <Form.Label>Fecha</Form.Label>

                  <Form.Control
                    required
                    type="date"
                    value={fecha}
                    onChange={(evento) => {
                      setFecha(evento.target.value);
                      setResultado(null);
                    }}
                  />
                </Form.Group>
              </Col>

              <Col md={3}>
                <Button
                  type="submit"
                  className="w-100"
                  disabled={
                    consultando ||
                    cargandoProfesionales ||
                    !profesionalSeleccionado
                  }
                >
                  {consultando ? (
                    <>
                      <Spinner
                        animation="border"
                        size="sm"
                        className="me-2"
                      />
                      Consultando...
                    </>
                  ) : (
                    'Consultar'
                  )}
                </Button>
              </Col>
            </Row>
          </Form>
        </Card.Body>
      </Card>

      {resultado && (
        <section>
          <div className="mb-3">
            <h2 className="h5 mb-1">
              Franjas disponibles
            </h2>

            <p className="text-secondary mb-0">
              {profesionalActual && (
                <>
                  {profesionalActual.nombre}{' '}
                  {profesionalActual.apellido}
                  {' · '}
                </>
              )}
              {mostrarFecha(resultado.fecha)}
            </p>
          </div>

          {resultado.franjas.length === 0 ? (
            <Alert variant="info">
              No existen franjas disponibles para la fecha
              seleccionada.
            </Alert>
          ) : (
            <div className="table-responsive">
              <Table
                striped
                bordered
                hover
                className="align-middle"
              >
                <thead>
                  <tr>
                    <th>Hora de inicio</th>
                    <th>Hora de finalización</th>
                    <th>Estado</th>
                  </tr>
                </thead>

                <tbody>
                  {resultado.franjas.map((franja) => (
                    <tr
                      key={`${franja.horaInicio}-${franja.horaFin}`}
                    >
                      <td>{mostrarHora(franja.horaInicio)}</td>
                      <td>{mostrarHora(franja.horaFin)}</td>
                      <td>
                        <span className="badge text-bg-success">
                          Disponible
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </Table>
            </div>
          )}
        </section>
      )}
    </main>
  );
}

export default DisponibilidadPage;