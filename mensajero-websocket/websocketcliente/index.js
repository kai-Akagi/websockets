// ---------- Configuración ----------
const URL_HTTP = 'http://localhost:8080';
const URL_WS = 'ws://localhost:8080/websocket';
const NOMBRE_VALIDO = /^[\p{L}\p{N}_. -]{1,20}$/u;

// ---------- Estado ----------
let stompCliente = null;   // cliente stomp
let miNombre = null;       // nombre con el que entré
let destinatario = null;   // null = todos; si no, nombre del usuario elegido
let usuarios = [];         // lista de usuarios conectados
let listaInicial = true;   // para no avisar "entró/salió" con la primera lista
let conectado = false;
let salidaVoluntaria = false;

const $ = (id) => document.getElementById(id);

// ---------- Conexión ----------
const entrar = async () => {
    const nombre = $('txtUsuario').value.trim();
    $('loginError').textContent = '';

    if (!NOMBRE_VALIDO.test(nombre) || nombre.toLowerCase() === 'todos') {
        $('loginError').textContent = 'Nombre inválido: usa de 1 a 20 letras, números, espacios, punto, guion o guion bajo (y no "Todos").';
        return;
    }

    // Avisa antes de conectar si el nombre ya está en uso
    try {
        const resp = await fetch(`${URL_HTTP}/api/usuarios/disponible?nombre=${encodeURIComponent(nombre)}`);
        const datos = await resp.json();
        if (!datos.disponible) {
            $('loginError').textContent = datos.motivo;
            return;
        }
    } catch (e) {
        $('loginError').textContent = 'No se pudo contactar al servidor. Verifica que esté en ejecución.';
        return;
    }

    conectarWS(nombre);
};

const conectarWS = (nombre) => {
    // Avoid having many open connections
    if (stompCliente !== null) {
        salidaVoluntaria = true;
        stompCliente.deactivate();
    }

    stompCliente = new StompJs.Client({
        webSocketFactory: () => new WebSocket(`${URL_WS}?nombre=${encodeURIComponent(nombre)}`),
        reconnectDelay: 0 // sin reconexión automática: el nombre quedaría en uso
    });
    stompCliente.onConnect = () => onConnectSocket(nombre);
    stompCliente.onWebSocketClose = onWebSocketClose;
    stompCliente.onStompError = (frame) => {
        mostrarSistema(`Error del servidor: ${frame.headers['message'] || 'desconocido'}`, true);
    };
    stompCliente.activate();
};

// function to suscribe to the broker
const onConnectSocket = (nombre) => {
    miNombre = nombre;
    conectado = true;
    listaInicial = true;
    destinatario = null;

    // Mensajes: públicos (todos) y privados (solo para mí)
    stompCliente.subscribe('/tema/mensajes', (m) => mostrarMensaje(JSON.parse(m.body)));
    stompCliente.subscribe('/user/queue/privado', (m) => mostrarMensaje(JSON.parse(m.body)));

    // Entidad Alumno: para todos y privada
    stompCliente.subscribe('/tema/entidades', (m) => agregarAlumno(JSON.parse(m.body)));
    stompCliente.subscribe('/user/queue/entidades', (m) => agregarAlumno(JSON.parse(m.body)));

    // Errores que el servidor me dirige a mí
    stompCliente.subscribe('/user/queue/errores', (m) => mostrarSistema(m.body, true));

    // Lista de usuarios: cambios en vivo + lista actual al suscribirse
    stompCliente.subscribe('/tema/usuarios', (m) => actualizarUsuarios(JSON.parse(m.body)));
    stompCliente.subscribe('/app/usuarios', (m) => actualizarUsuarios(JSON.parse(m.body)));

    $('miNombre').textContent = miNombre;
    $('ULMensajes').innerHTML = '';
    $('tbodyAlumnos').innerHTML = '';
    $('seccionLogin').classList.add('d-none');
    $('seccionApp').classList.remove('d-none');
    $('txtMensaje').focus();
};

// what to do when websocket closes
const onWebSocketClose = () => {
    const estabaConectado = conectado;
    conectado = false;
    if (salidaVoluntaria) {
        salidaVoluntaria = false;
        return;
    }
    if (stompCliente !== null) {
        stompCliente.deactivate();
        stompCliente = null;
    }
    mostrarLogin(estabaConectado
        ? 'Se perdió la conexión con el servidor.'
        : 'No se pudo conectar. Verifica que el servidor esté en ejecución y que el nombre no esté en uso.');
};

const salir = () => {
    if (stompCliente !== null) {
        salidaVoluntaria = true;
        stompCliente.deactivate();
        stompCliente = null;
    }
    conectado = false;
    mostrarLogin('');
};

const mostrarLogin = (error) => {
    usuarios = [];
    $('seccionApp').classList.add('d-none');
    $('seccionLogin').classList.remove('d-none');
    $('loginError').textContent = error;
    $('txtUsuario').focus();
};

// ---------- Usuarios conectados ----------
const actualizarUsuarios = (lista) => {
    if (!listaInicial) {
        lista.filter((u) => !usuarios.includes(u) && u !== miNombre)
            .forEach((u) => mostrarSistema(`${u} se conectó`));
        usuarios.filter((u) => !lista.includes(u))
            .forEach((u) => mostrarSistema(`${u} se desconectó`));
    }
    listaInicial = false;
    usuarios = lista;

    // Si el destinatario elegido se fue, se regresa a "Todos"
    if (destinatario !== null && !usuarios.includes(destinatario)) {
        destinatario = null;
    }
    renderUsuarios();
};

const renderUsuarios = () => {
    const ul = $('ULUsuarios');
    ul.innerHTML = '';
    ul.appendChild(crearItemUsuario(null, '👥 Todos'));
    usuarios.forEach((u) => ul.appendChild(crearItemUsuario(u, u === miNombre ? `${u} (tú)` : u)));
    $('badgeUsuarios').textContent = usuarios.length;
    $('destinoActual').textContent = destinatario === null ? 'Todos' : destinatario;
};

const crearItemUsuario = (valor, etiqueta) => {
    const li = document.createElement('li');
    li.className = 'list-group-item list-group-item-action usuario-item' + (valor === destinatario ? ' active' : '');
    li.textContent = etiqueta;
    li.addEventListener('click', () => {
        destinatario = valor;
        renderUsuarios();
    });
    return li;
};

// ---------- Mensajes ----------
// function to send message
const enviarMensaje = () => {
    const txtMensaje = $('txtMensaje');
    const contenido = txtMensaje.value.trim();
    if (!conectado || contenido === '') {
        return;
    }
    // publish message (destinatario null = todos)
    stompCliente.publish({
        destination: '/app/mensaje',
        body: JSON.stringify({ destinatario: destinatario, contenido: contenido })
    });
    txtMensaje.value = '';
    txtMensaje.focus();
};

// function to show message (textContent evita inyectar HTML)
const mostrarMensaje = (m) => {
    const propio = m.nombre === miNombre;
    let quien = propio ? 'Tú' : m.nombre;
    if (m.privado) {
        quien += propio ? ` → ${m.destinatario} (privado)` : ' (privado)';
    }

    const li = document.createElement('li');
    li.className = 'list-group-item' + (m.privado ? ' list-group-item-warning' : '');
    const strong = document.createElement('strong');
    strong.textContent = quien;
    const hora = document.createElement('small');
    hora.className = 'text-muted float-right';
    hora.textContent = m.hora;
    li.append(strong, `: ${m.contenido}`, hora);
    agregarALista(li);
};

const mostrarSistema = (texto, esError = false) => {
    const li = document.createElement('li');
    li.className = 'list-group-item small ' + (esError ? 'text-danger' : 'text-muted');
    li.textContent = texto;
    agregarALista(li);
};

const agregarALista = (li) => {
    const ul = $('ULMensajes');
    ul.appendChild(li);
    ul.scrollTop = ul.scrollHeight;
};

// ---------- Entidad: Alumno ----------
const enviarAlumno = () => {
    $('alumnoError').textContent = '';
    const alumno = {
        matricula: $('txtMatricula').value.trim(),
        nombre: $('txtNombreAlumno').value.trim(),
        carrera: $('txtCarrera').value.trim(),
        semestre: parseInt($('txtSemestre').value, 10)
    };

    if (!alumno.matricula || !alumno.nombre || !alumno.carrera) {
        $('alumnoError').textContent = 'Matrícula, nombre y carrera son obligatorios.';
        return;
    }
    if (!Number.isInteger(alumno.semestre) || alumno.semestre < 1 || alumno.semestre > 12) {
        $('alumnoError').textContent = 'El semestre debe ser un número entre 1 y 12.';
        return;
    }
    if (!conectado) {
        return;
    }

    // La entidad viaja como JSON: {destinatario, alumno:{matricula,nombre,carrera,semestre}}
    stompCliente.publish({
        destination: '/app/entidad',
        body: JSON.stringify({ destinatario: destinatario, alumno: alumno })
    });
    $('formAlumno').reset();
    $('txtMatricula').focus();
};

// Añade a la tabla HTML el alumno recibido
const agregarAlumno = (env) => {
    const propio = env.remitente === miNombre;
    let para = 'Todos';
    if (env.privado) {
        para = propio ? `${env.destinatario} (privado)` : 'Solo tú';
    }

    const tr = document.createElement('tr');
    [env.hora, propio ? 'Tú' : env.remitente, para,
        env.alumno.matricula, env.alumno.nombre, env.alumno.carrera, env.alumno.semestre
    ].forEach((valor) => {
        const td = document.createElement('td');
        td.textContent = valor;
        tr.appendChild(td);
    });
    $('tbodyAlumnos').appendChild(tr);
};

// Event when all DOM Content is loaded
document.addEventListener('DOMContentLoaded', () => {
    $('formLogin').addEventListener('submit', (e) => { e.preventDefault(); entrar(); });
    $('formMensaje').addEventListener('submit', (e) => { e.preventDefault(); enviarMensaje(); });
    $('formAlumno').addEventListener('submit', (e) => { e.preventDefault(); enviarAlumno(); });
    $('btnSalir').addEventListener('click', salir);
    $('txtUsuario').focus();
});
