import { useEffect, useState, type FormEvent } from 'react';
import { Alert, Badge, Button, Form, Modal, Spinner, Table } from 'react-bootstrap';
import { actualizarProfesional, asociarEspecialidad, consultarProfesional, crearProfesional, desactivarProfesional, listarEspecialidadesParaProfesionales, listarProfesionales } from '../../api/profesionales';
import type { UsuarioAutenticado } from '../../types/auth';
import { reactivarProfesional } from '../../api/profesionales';
import type { Especialidad } from '../../types/especialidad';
import type { Profesional, ProfesionalRequest } from '../../types/profesional';

interface Props { token: string; usuario: UsuarioAutenticado }
const VACIO: ProfesionalRequest = { nombre: '', apellido: '', matricula: '', telefono: '', email: '' };
const mensaje = (error: unknown) => error instanceof Error ? error.message : 'No se pudo completar la operación.';

export default function ProfesionalesPage({ token, usuario }: Props) {
  const administrador = usuario.rol === 'ADMINISTRADOR';
  const [profesionales, setProfesionales] = useState<Profesional[]>([]);
  const [especialidades, setEspecialidades] = useState<Especialidad[]>([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState('');
  const [exito, setExito] = useState('');
  const [intento, setIntento] = useState(0);
  const [modal, setModal] = useState<'datos' | 'especialidad' | 'baja' | 'reactivar' | null>(null);
  const [seleccionado, setSeleccionado] = useState<Profesional | null>(null);
  const [formulario, setFormulario] = useState<ProfesionalRequest>(VACIO);
  const [especialidadId, setEspecialidadId] = useState('');
  const [guardando, setGuardando] = useState(false);
  const [errorFormulario, setErrorFormulario] = useState('');

  useEffect(() => {
    let vigente = true;
    async function cargar() {
      try {
        if (usuario.rol === 'PROFESIONAL' && usuario.profesionalId === null) {
          throw new Error('Tu cuenta no tiene un profesional asociado. Consultá al administrador.');
        }
        const datos = usuario.rol === 'PROFESIONAL' && usuario.profesionalId !== null
          ? [await consultarProfesional(token, usuario.profesionalId)]
          : await listarProfesionales(token);
        if (vigente) setProfesionales(datos);
      } catch (err) {
        if (vigente) setError(mensaje(err));
      } finally { if (vigente) setCargando(false); }
    }
    void cargar();
    return () => { vigente = false; };
  }, [token, usuario.rol, usuario.profesionalId, intento]);

  function abrir(tipo: 'datos' | 'especialidad' | 'baja' | 'reactivar', profesional: Profesional | null = null) {
    if (!administrador) return;
    setSeleccionado(profesional);
    setFormulario(profesional ? {
      nombre: profesional.nombre, apellido: profesional.apellido, matricula: profesional.matricula,
      telefono: profesional.telefono ?? '', email: profesional.email ?? '',
    } : { ...VACIO });
    setEspecialidades([]);
    setEspecialidadId('');
    setErrorFormulario('');
    setExito('');
    setModal(tipo);
  }

  async function cargarEspecialidades(profesional: Profesional) {
    abrir('especialidad', profesional);
    setGuardando(true);
    try { setEspecialidades((await listarEspecialidadesParaProfesionales(token)).filter(e => e.activo && !profesional.especialidades?.some(asociada => asociada.id === e.id))); }
    catch (err) { setErrorFormulario(mensaje(err)); }
    finally { setGuardando(false); }
  }

  async function guardar(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault();
    if (!administrador || guardando) return;
    setErrorFormulario('');
    const datos: ProfesionalRequest = {
      nombre: formulario.nombre.trim(), apellido: formulario.apellido.trim(),
      matricula: formulario.matricula.trim(), telefono: formulario.telefono.trim(), email: formulario.email.trim(),
    };
    if (modal === 'datos' && (!datos.nombre || !datos.apellido || !datos.matricula)) {
      setErrorFormulario('Nombre, apellido y matrícula son obligatorios.'); return;
    }
    if (modal === 'especialidad' && !especialidadId) {
      setErrorFormulario('Seleccioná una especialidad.'); return;
    }
    setGuardando(true);
    try {
      if (modal === 'datos') {
        const resultado = seleccionado
          ? await actualizarProfesional(token, seleccionado.id, datos)
          : await crearProfesional(token, datos);
        setProfesionales(actuales => seleccionado ? actuales.map(p => p.id === resultado.id ? resultado : p) : [...actuales, resultado]);
        setExito('Profesional guardado correctamente.');
      } else if (modal === 'reactivar' && seleccionado) {
        const resultado = await reactivarProfesional(token, seleccionado.id);
        setProfesionales(actuales => actuales.map(p => p.id === resultado.id ? resultado : p));
        setExito('Profesional reactivado correctamente.');
      } else if (modal === 'baja' && seleccionado) {
        await desactivarProfesional(token, seleccionado.id);
        setProfesionales(actuales => actuales.map(p => p.id === seleccionado.id ? { ...p, activo: false } : p));
        setExito('Profesional desactivado correctamente.');
      } else if (modal === 'especialidad' && seleccionado) {
        await asociarEspecialidad(token, seleccionado.id, Number(especialidadId));
        const especialidad = especialidades.find(e => e.id === Number(especialidadId));
        if (especialidad) setProfesionales(actuales => actuales.map(p => p.id === seleccionado.id
          ? { ...p, especialidades: [...(p.especialidades ?? []), especialidad] } : p));
        setExito('Especialidad asociada correctamente.');
      }
      setModal(null);
    } catch (err) { setErrorFormulario(mensaje(err)); }
    finally { setGuardando(false); }
  }

  return <section aria-label="Profesionales">
    <div className="d-flex justify-content-between align-items-center mb-3">
      <h1 className="h3">{usuario.rol === 'PROFESIONAL' ? 'Mi perfil profesional' : 'Profesionales'}</h1>
      {administrador && <Button disabled={cargando} onClick={() => abrir('datos')}>Nuevo profesional</Button>}
    </div>
    {error && <Alert variant="danger">{error} <Button variant="outline-danger" onClick={() => { setError(''); setCargando(true); setIntento(i => i + 1); }}>Reintentar</Button></Alert>}
    {exito && <Alert variant="success" role="status">{exito}</Alert>}
    {cargando ? <div role="status"><Spinner size="sm" /> Cargando profesionales...</div> : !error && (
      profesionales.length === 0 ? <Alert variant="info">No hay profesionales para mostrar.</Alert> :
      <Table responsive striped bordered hover>
        <thead><tr><th>Nombre y apellido</th><th>Matrícula</th><th>Teléfono</th><th>Correo electrónico</th><th>Especialidades</th><th>Estado</th>{administrador && <th>Acciones</th>}</tr></thead>
        <tbody>{profesionales.map(p => <tr key={p.id}>
          <td>{p.nombre} {p.apellido}</td><td>{p.matricula}</td><td>{p.telefono || '—'}</td><td>{p.email || '—'}</td>
          <td>{p.especialidades?.length ? p.especialidades.map(e => `${e.nombre}${e.activo ? '' : ' (inactiva)'}`).join(', ') : 'Sin especialidades asociadas'}</td>
          <td><Badge bg={p.activo ? 'success' : 'secondary'}>{p.activo ? 'Activo' : 'Inactivo'}</Badge></td>
          {administrador && <td><div className="d-flex flex-wrap gap-2">
            <Button size="sm" variant="outline-primary" onClick={() => abrir('datos', p)}>Editar</Button>
            {!p.activo && <Button size="sm" variant="outline-success" onClick={() => abrir('reactivar', p)}>Reactivar</Button>}
            {p.activo && <><Button size="sm" variant="outline-secondary" onClick={() => void cargarEspecialidades(p)}>Asociar especialidad</Button>
              <Button size="sm" variant="outline-danger" onClick={() => abrir('baja', p)}>Desactivar</Button></>}
          </div></td>}
        </tr>)}</tbody>
      </Table>
    )}
    {administrador && <Modal show={modal !== null} onHide={() => { if (!guardando) setModal(null); }} backdrop={guardando ? 'static' : true} keyboard={!guardando}>
      <Form onSubmit={guardar}>
        <Modal.Header closeButton={!guardando}><Modal.Title>{modal === 'reactivar' ? 'Reactivar profesional' : modal === 'baja' ? 'Desactivar profesional' : modal === 'especialidad' ? 'Asociar especialidad' : seleccionado ? 'Editar profesional' : 'Nuevo profesional'}</Modal.Title></Modal.Header>
        <Modal.Body>
          {errorFormulario && <Alert variant="danger">{errorFormulario}</Alert>}
          <fieldset disabled={guardando}>
            {modal === 'datos' && (Object.keys(VACIO) as (keyof ProfesionalRequest)[]).map(campo => {
              const etiquetas = { nombre: 'Nombre', apellido: 'Apellido', matricula: 'Matrícula', telefono: 'Teléfono', email: 'Correo electrónico' };
              const limites = { nombre: 100, apellido: 100, matricula: 50, telefono: 30, email: 150 };
              return <Form.Group key={campo} controlId={`profesional-${campo}`} className="mb-3">
                <Form.Label>{etiquetas[campo]}{['nombre', 'apellido', 'matricula'].includes(campo) ? ' *' : ''}</Form.Label>
                <Form.Control type={campo === 'email' ? 'email' : campo === 'telefono' ? 'tel' : 'text'} required={['nombre', 'apellido', 'matricula'].includes(campo)} maxLength={limites[campo]} value={formulario[campo]} onChange={e => setFormulario(actual => ({ ...actual, [campo]: e.target.value }))} />
              </Form.Group>;
            })}
            {modal === 'baja' && <p>¿Querés desactivar a {seleccionado?.nombre} {seleccionado?.apellido}? Se conservará su registro como inactivo.</p>}
            {modal === 'reactivar' && <p>¿Querés reactivar a {seleccionado?.nombre} {seleccionado?.apellido}? Se conservarán sus datos, especialidades y horarios existentes.</p>}
            {modal === 'especialidad' && <Form.Group controlId="profesional-especialidad">
              <p>Profesional: {seleccionado?.nombre} {seleccionado?.apellido}</p>
              <Form.Label>Especialidad activa</Form.Label>
              <Form.Select required value={especialidadId} onChange={e => setEspecialidadId(e.target.value)}>
                <option value="">Seleccioná una especialidad</option>
                {especialidades.map(e => <option key={e.id} value={e.id}>{e.nombre}</option>)}
              </Form.Select>
              {!guardando && especialidades.length === 0 && <p className="mt-2">No hay especialidades activas nuevas para asociar. Si hubo un error, cerrá y volvé a abrir para reintentar.</p>}
            </Form.Group>}
          </fieldset>
        </Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" disabled={guardando} onClick={() => setModal(null)}>Cancelar</Button>
          <Button type="submit" variant={modal === 'baja' ? 'danger' : 'primary'} disabled={guardando || (modal === 'especialidad' && !especialidadId)}>
            {guardando ? <><Spinner size="sm" /> Procesando...</> : modal === 'reactivar' ? 'Reactivar' : modal === 'baja' ? 'Desactivar' : modal === 'especialidad' ? 'Asociar' : 'Guardar'}
          </Button>
        </Modal.Footer>
      </Form>
    </Modal>}
  </section>;
}
