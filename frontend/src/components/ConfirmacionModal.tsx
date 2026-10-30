import { Button, Modal } from 'react-bootstrap';

interface ConfirmacionModalProps {
  mostrar: boolean;
  titulo: string;
  mensaje: string;
  textoConfirmar: string;
  confirmando?: boolean;
  onCancelar: () => void;
  onConfirmar: () => void;
}

function ConfirmacionModal({
  mostrar,
  titulo,
  mensaje,
  textoConfirmar,
  confirmando = false,
  onCancelar,
  onConfirmar,
}: ConfirmacionModalProps) {
  return (
    <Modal
      show={mostrar}
      onHide={onCancelar}
      centered
      aria-labelledby="titulo-confirmacion"
    >
      <Modal.Header closeButton={!confirmando}>
        <Modal.Title id="titulo-confirmacion">
          {titulo}
        </Modal.Title>
      </Modal.Header>

      <Modal.Body>{mensaje}</Modal.Body>

      <Modal.Footer>
        <Button
          variant="light"
          onClick={onCancelar}
          disabled={confirmando}
        >
          Volver
        </Button>

        <Button
          variant="danger"
          onClick={onConfirmar}
          disabled={confirmando}
        >
          {confirmando ? 'Procesando…' : textoConfirmar}
        </Button>
      </Modal.Footer>
    </Modal>
  );
}

export default ConfirmacionModal;