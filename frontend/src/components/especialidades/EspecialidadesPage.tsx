import { useEffect, useState, type FormEvent } from 'react';
import { Alert, Button, Form, Modal, Spinner, Table } from 'react-bootstrap';
import type { Especialidad, EspecialidadFormData } from '../../types/especialidad';
import type { Rol } from '../../types/auth';
import {
  listarEspecialidades,
  crearEspecialidad,
  actualizarEspecialidad,
  eliminarEspecialidad,
} from '../../api/especialidades';

const FORM_VACIO: EspecialidadFormData = { nombre: '', descripcion: '' };

function EspecialidadesPage({ token, rol }: { token: string; rol: Rol }) {
  const administrador = rol === 'ADMINISTRADOR';
  const [especialidades, setEspecialidades] = useState<Especialidad[]>([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [mostrarModal, setMostrarModal] = useState(false);
  const [especialidadEditando, setEspecialidadEditando] = useState<Especialidad | null>(null);
  const [formulario, setFormulario] = useState<EspecialidadFormData>(FORM_VACIO);
  const [guardando, setGuardando] = useState(false);
  const [errorFormulario, setErrorFormulario] = useState<string | null>(null);

  useEffect(() => {
    let vigente = true;
    listarEspecialidades(token)
      .then(datos => { if (vigente) setEspecialidades(datos); })
      .catch(err => { if (vigente) setError(err instanceof Error ? err.message : 'No se pudieron cargar las especialidades.'); })
      .finally(() => { if (vigente) setCargando(false); });
    return () => { vigente = false; };
  }, [token]);

  async function cargarEspecialidades() {
    setCargando(true);
    setError(null);
    try {
      const datos = await listarEspecialidades(token);
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
    if (guardando) return;
    setMostrarModal(false);
  }

  async function guardarEspecialidad(evento: FormEvent) {
    evento.preventDefault();
    if (!administrador || guardando) return;
    const datos = { nombre: formulario.nombre.trim(), descripcion: formulario.descripcion.trim() };
    if (!datos.nombre) {
      setErrorFormulario('El nombre es obligatorio.');
      return;
    }
    setGuardando(true);
    setErrorFormulario(null);
    try {
      if (especialidadEditando) {
        await actualizarEspecialidad(token, especialidadEditando.id, datos);
      } else {
        await crearEspecialidad(token, datos);
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
    if (!administrador) return;
    const confirmar = window.confirm(`¿Dar de baja la especialidad "${especialidad.nombre}"?`);
    if (!confirmar) return;

    try {
      await eliminarEspecialidad(token, especialidad.id);
      await cargarEspecialidades();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'No se pudo dar de baja la especialidad.');
    }
  }

  return (
    <div className="container py-4">
      <div className="d-flex justify-content-between align-items-center mb-3">
        <h1 className="h3 mb-0">Especialidades</h1>
        {administrador && <Button onClick={abrirModalCrear}>Nueva especialidad</Button>}
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
              {administrador && <th>Acciones</th>}
            </tr>
          </thead>
          <tbody>
            {especialidades.map((especialidad) => (
              <tr key={especialidad.id}>
                <td>{especialidad.nombre}</td>
                <td>{especialidad.descripcion || '—'}</td>
                {administrador && <td className="text-end">
                  <Button
                    size="sm"
                    variant="outline-secondary"
                    className="me-2"
                    onClick={() => abrirModalEditar(especialidad)}
                  >
                    Editar
                  </Button>
                  <Button size="sm" variant="outline-danger" onClick={() => darDeBaja(especialidad)}>
                    Dar de baja
                  </Button>
                </td>}
              </tr>
            ))}
          </tbody>
        </Table>
      )}

      <Modal show={administrador && mostrarModal} onHide={cerrarModal} backdrop={guardando ? 'static' : true} keyboard={!guardando}>
        <Form onSubmit={guardarEspecialidad}>
          <Modal.Header closeButton={!guardando}>
            <Modal.Title>{especialidadEditando ? 'Editar especialidad' : 'Nueva especialidad'}</Modal.Title>
          </Modal.Header>
          <Modal.Body>
            {errorFormulario && <Alert variant="danger">{errorFormulario}</Alert>}
            <Form.Group className="mb-3">
              <Form.Label>Nombre</Form.Label>
              <Form.Control
                type="text"
                required
                maxLength={100}
                disabled={guardando}
                value={formulario.nombre}
                onChange={(e) => setFormulario({ ...formulario, nombre: e.target.value })}
              />
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label>Descripción</Form.Label>
              <Form.Control
                as="textarea"
                rows={3}
                maxLength={200}
                disabled={guardando}
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
