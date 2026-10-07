IMAGENES DEL PROGRAMA EN EJECUCION

menu principal
<img width="884" height="590" alt="image" src="https://github.com/user-attachments/assets/afa8709b-c0dd-43f9-b1a0-0dbbbf52acb1" />
PokeRancho
<img width="880" height="586" alt="image" src="https://github.com/user-attachments/assets/04160d0c-5297-4a1b-a488-a94fd3057af5" />
Enfrentamiento preparacion para el enfrentamiento 
<img width="883" height="590" alt="image" src="https://github.com/user-attachments/assets/cb28c136-027a-45ed-b11b-e06d50683bd5" />
Batalla pov jugador 1
<img width="894" height="596" alt="image" src="https://github.com/user-attachments/assets/cb983ff7-a571-4852-b0da-81f36b0e75fb" />
Batalla pov jugador 2
<img width="875" height="585" alt="image" src="https://github.com/user-attachments/assets/127e96e4-11bf-4cb9-8ff3-924b5ae5814e" />

Este laboratorio estuvo enfocado en el consumo de APIs y la manipulación de objetos JSON, aplicando una clara separación de responsabilidades mediante el patrón Observer. Esto se realizó con el fin de obtener un mayor control sobre el flujo de trabajo y mantener un orden claro en el proyecto.

El código se encuentra estructurado en los siguientes paquetes:

events: Contiene las interfaces encargadas de la comunicación orientada a eventos. Incluye componentes para notificar la lógica de combate, así como el GameControllerListener, el cual gestiona la lógica previa al duelo entre jugadores.

model: Aloja la lógica de negocio distribuida en varias clases:

Pokemon: Representa la entidad y estadísticas de los Pokémon en duelo.

Player: Representa la selección e información del usuario.

GameController: Funciona como el orquestador principal del sistema; asigna los Pokémon, inicia el flujo de trabajo y gestiona la carga masiva de datos.

BattleEngine: Constituye el núcleo del sistema. Funciona como una máquina de estados que gestiona los turnos y la interacción entre jugadores en la arena virtual.

Client: Contiene la lógica para realizar peticiones HTTP a la PokéAPI, permitiendo la consulta individual (por ID o nombre) y la descarga de Pokémon por lotes.

ui: Es la capa de presentación accesible para el usuario. Implementa un CardLayout para la navegación fluida entre pantallas (MenuPanel, BattlePanel, etc.), evitando el uso de ventanas o marcos emergentes (JDialog / JFrame adicionales).

Nota sobre el desarrollo: Durante la realización del laboratorio se emplearon modelos de lenguaje (LLMs) como Gemini 1.5 Flash y Claude 3.5 Sonnet para el refactorizado de código y la maquetación de la interfaz de usuario. Aunque se trata de una versión beta orientada principalmente al funcionamiento lógico del sistema (pudiendo presentar detalles estéticos menores), se veló por ofrecer una experiencia funcional e intuitiva.
