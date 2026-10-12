import { useEffect, useState, type FormEvent } from 'react';
import { Alert, Button, Form, Modal, Spinner, Table } from 'react-bootstrap';
import type { Especialidad, EspecialidadFormData } from '../../types/especialidad';
import {
  listarEspecialidades,
  crearEspecialidad,
  actualizarEspecialidad,
  eliminarEspecialidad,
} from '../../api/especialidades';

const FORM_VACIO: EspecialidadFormData = { nombre: '', descripcion: '' };

function EspecialidadesPage() {
  const [especialidades, setEspecialidades] = useState<Especialidad[]>([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [mostrarModal, setMostrarModal] = useState(false);
  const [especialidadEditando, setEspecialidadEditando] = useState<Especialidad | null>(null);
  const [formulario, setFormulario] = useState<EspecialidadFormData>(FORM_VACIO);
  const [guardando, setGuardando] = useState(false);
  const [errorFormulario, setErrorFormulario] = useState<string | null>(null);

  useEffect(() => {
    cargarEspecialidades();
  }, []);

  async function cargarEspecialidades() {
    setCargando(true);
    setError(null);
    try {
      const datos = await listarEspecialidades();
      setEspecialidades(datos);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'No se pudieron cargar las especialidades.');
    } finally {
      setCargando(false);
    }
  }

  function abrirModalCrear() {
    setEspecialidadEditando(null);
    setFormulario(FORM_VACIO);
    setErrorFormulario(null);
    setMostrarModal(true);
  }

  function abrirModalEditar(especialidad: Especialidad) {
    setEspecialidadEditando(especialidad);
    setFormulario({ nombre: especialidad.nombre, descripcion: especialidad.descripcion ?? '' });
    setErrorFormulario(null);
    setMostrarModal(true);
  }

  function cerrarModal() {
    setMostrarModal(false);
  }

  async function guardarEspecialidad(evento: FormEvent) {
    evento.preventDefault();
    setGuardando(true);
    setErrorFormulario(null);
    try {
      if (especialidadEditando) {
        await actualizarEspecialidad(especialidadEditando.id, formulario);
      } else {
        await crearEspecialidad(formulario);
      }
      setMostrarModal(false);
      await cargarEspecialidades();
    } catch (err) {
      setErrorFormulario(err instanceof Error ? err.message : 'No se pudo guardar la especialidad.');
    } finally {
      setGuardando(false);
    }
  }

  async function darDeBaja(especialidad: Especialidad) {
    const confirmar = window.confirm(`¿Dar de baja la especialidad "${especialidad.nombre}"?`);
    if (!confirmar) return;

    try {
      await eliminarEspecialidad(especialidad.id);
      await cargarEspecialidades();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'No se pudo dar de baja la especialidad.');
    }
  }

  return (
    <div className="container py-4">
      <div className="d-flex justify-content-between align-items-center mb-3">
        <h1 className="h3 mb-0">Especialidades</h1>
        <Button onClick={abrirModalCrear}>Nueva especialidad</Button>
      </div>

      {error && (
        <Alert variant="danger" onClose={() => setError(null)} dismissible>
          {error}
        </Alert>
      )}

      {cargando ? (
        <div className="text-center py-5">
          <Spinner animation="border" role="status" />
        </div>
      ) : especialidades.length === 0 ? (
        <p className="text-muted">No hay especialidades cargadas todavía.</p>
      ) : (
        <Table striped bordered hover responsive>
          <thead>
            <tr>
              <th>Nombre</th>
              <th>Descripción</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {especialidades.map((especialidad) => (
              <tr key={especialidad.id}>
                <td>{especialidad.nombre}</td>
                <td>{especialidad.descripcion || '—'}</td>
                <td className="text-end">
                  <Button
                    size="sm"
                    variant="outline-secondary"
                    className="me-2"
                    onClick={() => abrirModalEditar(especialidad)}
                  >
                    Editar
                  </Button>
                  <Button
                    size="sm"
                    variant="outline-secondary"
                    className="me-2"
                    disabled
                    title="Se habilita cuando esté el endpoint de asociación profesional-especialidad"
                  >
                    Profesionales asociados
                  </Button>
                  <Button size="sm" variant="outline-danger" onClick={() => darDeBaja(especialidad)}>
                    Dar de baja
                  </Button>
                </td>
              </tr>
            ))}
          </tbody>
        </Table>
      )}

      <Modal show={mostrarModal} onHide={cerrarModal}>
        <Form onSubmit={guardarEspecialidad}>
          <Modal.Header closeButton>
            <Modal.Title>{especialidadEditando ? 'Editar especialidad' : 'Nueva especialidad'}</Modal.Title>
          </Modal.Header>
          <Modal.Body>
            {errorFormulario && <Alert variant="danger">{errorFormulario}</Alert>}
            <Form.Group className="mb-3">
              <Form.Label>Nombre</Form.Label>
              <Form.Control
                type="text"
                required
                value={formulario.nombre}
                onChange={(e) => setFormulario({ ...formulario, nombre: e.target.value })}
              />
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label>Descripción</Form.Label>
              <Form.Control
                as="textarea"
                rows={3}
                value={formulario.descripcion}
                onChange={(e) => setFormulario({ ...formulario, descripcion: e.target.value })}
              />
            </Form.Group>
          </Modal.Body>
          <Modal.Footer>
            <Button variant="secondary" onClick={cerrarModal} disabled={guardando}>
              Cancelar
            </Button>
            <Button type="submit" disabled={guardando}>
              {guardando ? 'Guardando...' : 'Guardar'}
            </Button>
          </Modal.Footer>
        </Form>
      </Modal>
    </div>
  );
}

export default EspecialidadesPage;
