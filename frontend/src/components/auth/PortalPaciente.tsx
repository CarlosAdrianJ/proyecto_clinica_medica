import { useEffect, useState } from 'react';
import {
  Alert,
  Card,
  Col,
  Container,
  Row,
  Spinner,
  Table,
} from 'react-bootstrap';
import { consultarMiHistorial } from '../../api/turnos';
import type { Paciente } from '../../types/paciente';
import type { Turno } from '../../types/turno';

const API_URL = `${import.meta.env?.VITE_API_URL ?? ''}/api/pacientes/mi-perfil`;

interface PortalPacienteProps {
  token: string;
}

function PortalPaciente({ token }: PortalPacienteProps) {
  const [perfil, setPerfil] = useState<Paciente | null>(null);
  const [turnos, setTurnos] = useState<Turno[]>([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let componenteActivo = true;

    async function cargarDatos() {
      try {
        const respuestaPerfil = await fetch(API_URL, {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });

        if (!respuestaPerfil.ok) {
          throw new Error('No se pudo cargar tu ficha de paciente.');
        }

        const datosPerfil =
          (await respuestaPerfil.json()) as Paciente;

        const historial = await consultarMiHistorial(token);

        if (componenteActivo) {
          setPerfil(datosPerfil);
          setTurnos(historial);
        }
      } catch (err) {
        if (componenteActivo) {
          setError(
            err instanceof Error
              ? err.message
              : 'No se pudieron cargar tus datos.',
          );
        }
      } finally {
        if (componenteActivo) {
          setCargando(false);
        }
      }
    }

    void cargarDatos();

    return () => {
      componenteActivo = false;
    };
  }, [token]);

  if (cargando) {
    return (
      <Container className="py-5 text-center">
        <Spinner animation="border" />
        <p className="mt-3">Cargando tu información...</p>
      </Container>
    );
  }

  if (error) {
    return (
      <Container className="py-4">
        <Alert variant="danger">{error}</Alert>
      </Container>
    );
  }

  if (!perfil) {
    return null;
  }

  return (
    <Container className="py-4">
      <h1 className="mb-4">Mi espacio de paciente</h1>

      <Card className="mb-4 shadow-sm border-0">
        <Card.Body>
          <Card.Title>Mis datos</Card.Title>

          <Row>
            <Col md={6}>
              <p>
                <strong>Nombre:</strong> {perfil.nombre}{' '}
                {perfil.apellido}
              </p>
              <p>
                <strong>DNI:</strong> {perfil.dni}
              </p>
              <p>
                <strong>Fecha de nacimiento:</strong>{' '}
                {perfil.fechaNacimiento ?? 'No informada'}
              </p>
            </Col>

            <Col md={6}>
              <p>
                <strong>Correo:</strong>{' '}
                {perfil.email ?? 'No informado'}
              </p>
              <p>
                <strong>Teléfono:</strong>{' '}
                {perfil.telefono ?? 'No informado'}
              </p>
              <p>
                <strong>Dirección:</strong>{' '}
                {perfil.direccion ?? 'No informada'}
              </p>
            </Col>
          </Row>
        </Card.Body>
      </Card>

      <Card className="shadow-sm border-0">
        <Card.Body>
          <Card.Title>Mis turnos</Card.Title>

          {turnos.length === 0 ? (
            <Alert variant="info" className="mb-0">
              Todavía no tenés turnos registrados.
            </Alert>
          ) : (
            <Table responsive hover className="align-middle">
              <thead>
                <tr>
                  <th>Fecha</th>
                  <th>Horario</th>
                  <th>Profesional</th>
                  <th>Especialidad</th>
                  <th>Estado</th>
                </tr>
              </thead>

              <tbody>
                {turnos.map((turno) => (
                  <tr key={turno.id}>
                    <td>{turno.fecha}</td>
                    <td>
                      {turno.horaInicio.slice(0, 5)} a{' '}
                      {turno.horaFin.slice(0, 5)}
                    </td>
                    <td>{turno.profesionalNombre}</td>
                    <td>{turno.especialidadNombre}</td>
                    <td>{turno.estado}</td>
                  </tr>
                ))}
              </tbody>
            </Table>
          )}
        </Card.Body>
      </Card>
    </Container>
  );
}

export default PortalPaciente;