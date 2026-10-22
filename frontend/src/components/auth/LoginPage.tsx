import { useState, type FormEvent } from "react";
import { Alert, Button, Card, Form, Spinner } from "react-bootstrap";
import { iniciarSesion } from "../../api/auth";
import type { LoginResponse } from "../../types/auth";

interface LoginPageProps {
  onLogin: (respuesta: LoginResponse) => void;
}

function LoginPage({ onLogin }: LoginPageProps) {
  const [nombreUsuario, setNombreUsuario] = useState("");
  const [contrasena, setContrasena] = useState("");
  const [ingresando, setIngresando] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function enviarFormulario(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault();
    setIngresando(true);
    setError(null);

    try {
      const respuesta = await iniciarSesion({
        nombreUsuario: nombreUsuario.trim(),
        contrasena,
      });

      onLogin(respuesta);
    } catch (err) {
      setError(
        err instanceof Error ? err.message : "No se pudo iniciar sesión.",
      );
    } finally {
      setIngresando(false);
    }
  }

  return (
    <main className="min-vh-100 d-flex align-items-center bg-light">
      <div className="container">
        <div className="row justify-content-center">
          <div className="col-12 col-sm-10 col-md-7 col-lg-5">
            <Card className="border-0 shadow-sm">
              <Card.Body className="p-4 p-md-5">
                <div className="text-center mb-4">
                  <h1 className="h3 mb-2">Clínica Médica</h1>
                  <p className="text-secondary mb-0">
                    Ingrese sus credenciales para continuar
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

                <Form onSubmit={enviarFormulario}>
                  <Form.Group className="mb-3" controlId="nombreUsuario">
                    <Form.Label>Usuario</Form.Label>
                    <Form.Control
                      type="text"
                      value={nombreUsuario}
                      onChange={(evento) =>
                        setNombreUsuario(evento.target.value)
                      }
                      maxLength={50}
                      autoComplete="username"
                      required
                      autoFocus
                    />
                  </Form.Group>

                  <Form.Group className="mb-4" controlId="contrasena">
                    <Form.Label>Contraseña</Form.Label>
                    <Form.Control
                      type="password"
                      value={contrasena}
                      onChange={(evento) => setContrasena(evento.target.value)}
                      maxLength={72}
                      autoComplete="current-password"
                      required
                    />
                  </Form.Group>

                  <Button
                    type="submit"
                    className="w-100"
                    disabled={
                      ingresando ||
                      nombreUsuario.trim() === "" ||
                      contrasena === ""
                    }
                  >
                    {ingresando ? (
                      <>
                        <Spinner
                          animation="border"
                          size="sm"
                          className="me-2"
                        />
                        Ingresando...
                      </>
                    ) : (
                      "Ingresar"
                    )}
                  </Button>
                </Form>
              </Card.Body>
            </Card>
          </div>
        </div>
      </div>
    </main>
  );
}

export default LoginPage;
