import { useState, type FormEvent } from "react";
import { Alert, Button, Card, Form, Spinner } from "react-bootstrap";
import {
  iniciarSesion,
  registrarPaciente,
  type RegistroPaciente,
} from "../../api/auth";
import type { LoginResponse } from "../../types/auth";

interface LoginPageProps {
  onLogin: (respuesta: LoginResponse) => void;
}

const REGISTRO_VACIO: RegistroPaciente = {
  nombre: "",
  apellido: "",
  dni: "",
  fechaNacimiento: null,
  telefono: "",
  email: "",
  direccion: "",
  nombreUsuario: "",
  contrasena: "",
};

function LoginPage({ onLogin }: LoginPageProps) {
  const [nombreUsuario, setNombreUsuario] = useState("");
  const [contrasena, setContrasena] = useState("");
  const [datosPaciente, setDatosPaciente] =
    useState<RegistroPaciente>(REGISTRO_VACIO);
  const [registrando, setRegistrando] = useState(false);
  const [ingresando, setIngresando] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function enviarFormulario(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault();
    setIngresando(true);
    setError(null);

    try {
      const respuesta = registrando
        ? await registrarPaciente(datosPaciente)
        : await iniciarSesion({
            nombreUsuario: nombreUsuario.trim(),
            contrasena,
          });

      onLogin(respuesta);
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : "No se pudo completar la operación.",
      );
    } finally {
      setIngresando(false);
    }
  }

  function actualizarPaciente(
    campo: keyof RegistroPaciente,
    valor: string | null,
  ) {
    setDatosPaciente((actual) => ({
      ...actual,
      [campo]: valor,
    }));
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
                    {registrando
                      ? "Creá tu cuenta para acceder a tu ficha y tus turnos."
                      : "Ingresá con tu usuario de paciente, profesional o del equipo administrativo."}
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
                  {registrando && (
                    <>
                      <Form.Group className="mb-3">
                        <Form.Label>Nombre</Form.Label>
                        <Form.Control
                          required
                          maxLength={100}
                          value={datosPaciente.nombre}
                          onChange={(evento) =>
                            actualizarPaciente("nombre", evento.target.value)
                          }
                        />
                      </Form.Group>

                      <Form.Group className="mb-3">
                        <Form.Label>Apellido</Form.Label>
                        <Form.Control
                          required
                          maxLength={100}
                          value={datosPaciente.apellido}
                          onChange={(evento) =>
                            actualizarPaciente("apellido", evento.target.value)
                          }
                        />
                      </Form.Group>

                      <Form.Group className="mb-3">
                        <Form.Label>DNI</Form.Label>
                        <Form.Control
                          required
                          maxLength={20}
                          value={datosPaciente.dni}
                          onChange={(evento) =>
                            actualizarPaciente("dni", evento.target.value)
                          }
                        />
                      </Form.Group>

                      <Form.Group className="mb-3">
                        <Form.Label>Fecha de nacimiento</Form.Label>
                        <Form.Control
                          type="date"
                          value={datosPaciente.fechaNacimiento ?? ""}
                          onChange={(evento) =>
                            actualizarPaciente(
                              "fechaNacimiento",
                              evento.target.value || null,
                            )
                          }
                        />
                      </Form.Group>

                      <Form.Group className="mb-3">
                        <Form.Label>Correo electrónico</Form.Label>
                        <Form.Control
                          type="email"
                          required
                          maxLength={150}
                          autoComplete="email"
                          value={datosPaciente.email}
                          onChange={(evento) =>
                            actualizarPaciente("email", evento.target.value)
                          }
                        />
                      </Form.Group>

                      <Form.Group className="mb-3">
                        <Form.Label>Teléfono</Form.Label>
                        <Form.Control
                          maxLength={30}
                          value={datosPaciente.telefono}
                          onChange={(evento) =>
                            actualizarPaciente("telefono", evento.target.value)
                          }
                        />
                      </Form.Group>

                      <Form.Group className="mb-3">
                        <Form.Label>Dirección</Form.Label>
                        <Form.Control
                          maxLength={200}
                          value={datosPaciente.direccion}
                          onChange={(evento) =>
                            actualizarPaciente("direccion", evento.target.value)
                          }
                        />
                      </Form.Group>
                    </>
                  )}

                  <Form.Group className="mb-3">
                    <Form.Label>Usuario</Form.Label>
                    <Form.Control
                      required
                      type="text"
                      minLength={registrando ? 4 : undefined}
                      maxLength={50}
                      autoComplete="username"
                      value={
                        registrando
                          ? datosPaciente.nombreUsuario
                          : nombreUsuario
                      }
                      onChange={(evento) => {
                        if (registrando) {
                          actualizarPaciente(
                            "nombreUsuario",
                            evento.target.value,
                          );
                        } else {
                          setNombreUsuario(evento.target.value);
                        }
                      }}
                    />
                  </Form.Group>

                  <Form.Group className="mb-4">
                    <Form.Label>Contraseña</Form.Label>
                    <Form.Control
                      required
                      type="password"
                      minLength={registrando ? 10 : undefined}
                      maxLength={72}
                      autoComplete={
                        registrando ? "new-password" : "current-password"
                      }
                      value={
                        registrando ? datosPaciente.contrasena : contrasena
                      }
                      onChange={(evento) => {
                        if (registrando) {
                          actualizarPaciente("contrasena", evento.target.value);
                        } else {
                          setContrasena(evento.target.value);
                        }
                      }}
                    />
                    {registrando && (
                      <Form.Text muted>Usá al menos 10 caracteres.</Form.Text>
                    )}
                  </Form.Group>

                  <Button
                    type="submit"
                    className="w-100"
                    disabled={
                      ingresando ||
                      (registrando
                        ? !datosPaciente.nombre ||
                          !datosPaciente.apellido ||
                          !datosPaciente.dni ||
                          !datosPaciente.email ||
                          datosPaciente.nombreUsuario.length < 4 ||
                          datosPaciente.contrasena.length < 10
                        : !nombreUsuario.trim() || !contrasena)
                    }
                  >
                    {ingresando ? (
                      <>
                        <Spinner
                          animation="border"
                          size="sm"
                          className="me-2"
                        />
                        {registrando ? "Creando cuenta..." : "Ingresando..."}
                      </>
                    ) : registrando ? (
                      "Crear cuenta de paciente"
                    ) : (
                      "Ingresar"
                    )}
                  </Button>

                  <div className="text-center mt-3">
                    <Button
                      variant="link"
                      type="button"
                      onClick={() => {
                        setRegistrando((actual) => !actual);
                        setError(null);
                      }}
                    >
                      {registrando
                        ? "Ya tengo una cuenta: ingresar"
                        : "¿Sos paciente? Crear cuenta"}
                    </Button>
                  </div>
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
