import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;


public class Main {
    private static final String DATA_FILE = "events.data";
    static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static void main(String[] args) {
        try {
            System.setOut(new java.io.PrintStream(System.out, true, "UTF-8"));
        } catch (Exception e) {
            // Ignorar se não conseguir mudar encoding
        }
        Scanner sc = new Scanner(System.in);
        SistemaEventos sistema = SistemaEventos.loadFromFile(DATA_FILE);

        System.out.println("=== Sistema de Eventos (Console) ===");

        int opcao;
        do {
            showMenu();
            opcao = readInt(sc, "Escolha uma opção: ");
            switch (opcao) {
                case 1:
                    cadastrarUsuario(sc, sistema);
                    sistema.saveToFile(DATA_FILE);
                    break;
                case 2:
                    cadastrarEvento(sc, sistema);
                    sistema.saveToFile(DATA_FILE);
                    break;
                case 3:
                    listarEventos(sc, sistema);
                    break;
                case 4:
                    participarEvento(sc, sistema);
                    sistema.saveToFile(DATA_FILE);
                    break;
                case 5:
                    cancelarParticipacao(sc, sistema);
                    sistema.saveToFile(DATA_FILE);
                    break;
                case 6:
                    mostrarEventosConfirmados(sc, sistema);
                    break;
                case 7:
                    ordenarEventosPorData(sc, sistema);
                    sistema.saveToFile(DATA_FILE);
                    break;
                case 8:
                    verificarStatusEvento(sc, sistema);
                    break;
                case 0:
                    System.out.println("Saindo. Salvando dados...");
                    sistema.saveToFile(DATA_FILE);
                    break;
                default:
                    System.out.println("Opção inválida!");
            }
            System.out.println();
        } while (opcao != 0);

        sc.close();
    }

    private static void showMenu() {
        System.out.println("1. Cadastrar usuário");
        System.out.println("2. Cadastrar evento");
        System.out.println("3. Listar eventos");
        System.out.println("4. Participar de evento");
        System.out.println("5. Cancelar participação");
        System.out.println("6. Mostrar meus eventos confirmados");
        System.out.println("7. Ordenar eventos por data/hora");
        System.out.println("8. Verificar se evento está ocorrendo/agora ou já passou");
        System.out.println("0. Sair");
    }

    private static int readInt(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = sc.nextLine().trim();
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                System.out.println("Entrada inválida. Digite um número inteiro.");
            }
        }
    }

    private static void cadastrarUsuario(Scanner sc, SistemaEventos sistema) {
        System.out.println("=== Cadastrar Usuário ===");
        System.out.print("Nome: ");
        String nome = sc.nextLine().trim();
        System.out.print("Email: ");
        String email = sc.nextLine().trim().toLowerCase();
        int idade = readInt(sc, "Idade: ");

        if (sistema.findUsuarioByEmail(email) != null) {
            System.out.println("Usuário com esse email já cadastrado.");
            return;
        }

        Usuario u = new Usuario(nome, email, idade);
        sistema.addUsuario(u);
        System.out.println("Usuário cadastrado com sucesso.");
    }

    private static void cadastrarEvento(Scanner sc, SistemaEventos sistema) {
        System.out.println("=== Cadastrar Evento ===");
        System.out.print("Nome do evento: ");
        String nome = sc.nextLine().trim();
        System.out.print("Endereço: ");
        String endereco = sc.nextLine().trim();
        System.out.print("Categoria (ex.: show, esporte, cultura): ");
        String categoria = sc.nextLine().trim();
        System.out.print("Descrição: ");
        String descricao = sc.nextLine().trim();

        LocalDateTime dt;
        while (true) {
            System.out.print("Data e hora (formato yyyy-MM-dd HH:mm): ");
            String s = sc.nextLine().trim();
            try {
                dt = LocalDateTime.parse(s, DATE_FORMAT);
                break;
            } catch (DateTimeParseException e) {
                System.out.println("Formato inválido. Exemplo válido: 2025-12-31 20:30");
            }
        }

        Evento e = sistema.createEvento(nome, endereco, categoria, dt, descricao);
        System.out.println("Evento criado com ID: " + e.getId());
    }

    private static void listarEventos(Scanner sc, SistemaEventos sistema) {
        System.out.println("=== Listar Eventos ===");
        List<Evento> eventos = sistema.getEventos();
        if (eventos.isEmpty()) {
            System.out.println("Nenhum evento cadastrado.");
            return;
        }
        for (Evento e : eventos) {
            System.out.println(e.briefInfo());
        }
    }

    private static void participarEvento(Scanner sc, SistemaEventos sistema) {
        System.out.println("=== Participar de Evento ===");
        System.out.print("Seu email: ");
        String email = sc.nextLine().trim().toLowerCase();
        Usuario u = sistema.findUsuarioByEmail(email);
        if (u == null) {
            System.out.println("Usuário não encontrado. Deseja cadastrar? (s/n)");
            String r = sc.nextLine().trim().toLowerCase();
            if (r.equals("s")) {
                cadastrarUsuario(sc, sistema);
                u = sistema.findUsuarioByEmail(email);
                if (u == null) {
                    System.out.println("Ainda não cadastrado. Operação cancelada.");
                    return;
                }
            } else {
                return;
            }
        }

        System.out.print("ID do evento (numero): ");
        String id = sc.nextLine().trim();
        Evento ev = sistema.findEventoById(id);
        if (ev == null) {
            System.out.println("Evento não encontrado.");
            return;
        }
        boolean ok = sistema.participarEvento(email, id);
        if (ok) {
            System.out.println("Você foi inscrito no evento.");
        } else {
            System.out.println("Você já estava inscrito neste evento.");
        }
    }

    private static void cancelarParticipacao(Scanner sc, SistemaEventos sistema) {
        System.out.println("=== Cancelar Participação ===");
        System.out.print("Seu email: ");
        String email = sc.nextLine().trim().toLowerCase();
        Usuario u = sistema.findUsuarioByEmail(email);
        if (u == null) {
            System.out.println("Usuário não encontrado.");
            return;
        }
        System.out.print("ID do evento: ");
        String id = sc.nextLine().trim();
        Evento ev = sistema.findEventoById(id);
        if (ev == null) {
            System.out.println("Evento não encontrado.");
            return;
        }
        boolean ok = sistema.cancelarParticipacao(email, id);
        if (ok) {
            System.out.println("Participação cancelada.");
        } else {
            System.out.println("Você não estava inscrito neste evento.");
        }
    }

    private static void mostrarEventosConfirmados(Scanner sc, SistemaEventos sistema) {
        System.out.println("=== Meus Eventos Confirmados ===");
        System.out.print("Seu email: ");
        String email = sc.nextLine().trim().toLowerCase();
        Usuario u = sistema.findUsuarioByEmail(email);
        if (u == null) {
            System.out.println("Usuário não encontrado.");
            return;
        }
        List<Evento> list = sistema.getEventosDoUsuario(email);
        if (list.isEmpty()) {
            System.out.println("Você não confirmou presença em nenhum evento.");
            return;
        }
        for (Evento e : list) {
            System.out.println(e.briefInfo());
        }
    }

    private static void ordenarEventosPorData(Scanner sc, SistemaEventos sistema) {
        sistema.sortByDate();
        System.out.println("Eventos ordenados por data/hora (mais próximos primeiro).");
    }

    private static void verificarStatusEvento(Scanner sc, SistemaEventos sistema) {
        System.out.println("=== Verificar Status de Evento ===");
        System.out.print("ID do evento: ");
        String id = sc.nextLine().trim();
        Evento ev = sistema.findEventoById(id);
        if (ev == null) {
            System.out.println("Evento não encontrado.");
            return;
        }
        String status = sistema.getStatusEvento(ev);
        System.out.println("Evento: " + ev.getNome());
        System.out.println("Data/Hora: " + ev.getDateTime().format(DATE_FORMAT));
        System.out.println("Status: " + status);
    }
}

class Usuario implements Serializable {
    private static final long serialVersionUID = 1L;
    private String nome;
    private String email;
    private int idade;

    public Usuario(String nome, String email, int idade) {
        this.nome = nome;
        this.email = email.toLowerCase();
        this.idade = idade;
    }

    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public int getIdade() { return idade; }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Usuario)) return false;
        return ((Usuario) o).email.equalsIgnoreCase(this.email);
    }

    @Override
    public int hashCode() {
        return email.toLowerCase().hashCode();
    }

    @Override
    public String toString() {
        return "Usuario{" + nome + ", " + email + ", " + idade + "}";
    }
}

class Evento implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id; // string id (incremental convertido para String)
    private String nome;
    private String endereco;
    private String categoria;
    private LocalDateTime dateTime;
    private String descricao;
    private Set<String> participantesEmails = new HashSet<>();

    public Evento(String id, String nome, String endereco, String categoria, LocalDateTime dateTime, String descricao) {
        this.id = id;
        this.nome = nome;
        this.endereco = endereco;
        this.categoria = categoria;
        this.dateTime = dateTime;
        this.descricao = descricao;
    }

    public String getId() { return id; }
    public String getNome() { return nome; }
    public String getEndereco() { return endereco; }
    public String getCategoria() { return categoria; }
    public LocalDateTime getDateTime() { return dateTime; }
    public String getDescricao() { return descricao; }
    public Set<String> getParticipantesEmails() { return participantesEmails; }

    public void addParticipante(String email) { participantesEmails.add(email.toLowerCase()); }
    public boolean removeParticipante(String email) { return participantesEmails.remove(email.toLowerCase()); }
    public boolean isParticipante(String email) { return participantesEmails.contains(email.toLowerCase()); }
    public int qtdParticipantes() { return participantesEmails.size(); }

    public String briefInfo() {
        return String.format("ID:%s | %s | %s | %s | participantes: %d",
                id, nome, dateTime.format(Main.DATE_FORMAT), categoria, qtdParticipantes());
    }

    @Override
    public String toString() {
        return "Evento{" + id + "," + nome + "," + dateTime.format(Main.DATE_FORMAT) + "}";
    }
}

class SistemaEventos implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<Usuario> usuarios = new ArrayList<>();
    private List<Evento> eventos = new ArrayList<>();
    private long nextEventId = 1;

    // CRUD básico
    public void addUsuario(Usuario u) {
        usuarios.add(u);
    }
    public Usuario findUsuarioByEmail(String email) {
        if (email == null) return null;
        for (Usuario u : usuarios) if (u.getEmail().equalsIgnoreCase(email)) return u;
        return null;
    }

    public Evento createEvento(String nome, String endereco, String categoria, LocalDateTime dt, String descricao) {
        String id = String.valueOf(nextEventId++);
        Evento e = new Evento(id, nome, endereco, categoria, dt, descricao);
        eventos.add(e);
        return e;
    }

    public List<Evento> getEventos() {
        return eventos;
    }

    public Evento findEventoById(String id) {
        if (id == null) return null;
        for (Evento e : eventos) if (e.getId().equals(id)) return e;
        return null;
    }

    public boolean participarEvento(String email, String eventId) {
        Evento e = findEventoById(eventId);
        if (e == null) return false;
        if (e.isParticipante(email)) return false;
        e.addParticipante(email);
        return true;
    }

    public boolean cancelarParticipacao(String email, String eventId) {
        Evento e = findEventoById(eventId);
        if (e == null) return false;
        return e.removeParticipante(email);
    }

    public List<Evento> getEventosDoUsuario(String email) {
        List<Evento> r = new ArrayList<>();
        for (Evento e : eventos) {
            if (e.isParticipante(email)) r.add(e);
        }
        return r;
    }

    public void sortByDate() {
        Collections.sort(eventos, Comparator.comparing(Evento::getDateTime));
    }

    public String getStatusEvento(Evento e) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = e.getDateTime();
        LocalDateTime end = start.plusHours(2); // suposição de duração 2h
        if (!now.isBefore(start) && now.isBefore(end)) return "Acontecendo agora";
        if (now.isBefore(start)) return "Acontecerá futuramente";
        return "Já passou";
    }

    public void saveToFile(String filename) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(this);
        } catch (IOException ex) {
            System.out.println("Erro ao salvar dados: " + ex.getMessage());
        }
    }

    public static SistemaEventos loadFromFile(String filename) {
        File f = new File(filename);
        if (!f.exists()) {
            System.out.println("Nenhum arquivo de dados encontrado. Iniciando novo sistema.");
            return new SistemaEventos();
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            Object obj = ois.readObject();
            if (obj instanceof SistemaEventos) {
                System.out.println("Dados carregados de " + filename);
                return (SistemaEventos) obj;
            } else {
                System.out.println("Arquivo de dados inválido. Iniciando novo sistema.");
                return new SistemaEventos();
            }
        } catch (Exception ex) {
            System.out.println("Falha ao carregar dados: " + ex.getMessage());
            return new SistemaEventos();
        }
    }
}
