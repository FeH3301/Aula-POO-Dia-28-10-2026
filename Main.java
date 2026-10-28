import java.util.ArrayList;
import java.util.List;

interface Desconto {
    double aplicar(double valor);
}

class SemDesconto implements Desconto {
    public double aplicar(double valor) { return valor; }
}

class DescontoEstudante implements Desconto {
    public double aplicar(double valor) { return valor * 0.90; }
}

class DescontoPromocional20 implements Desconto {
    public double aplicar(double valor) { return valor * 0.80; }
}

final class Configuracao {
    private static final Configuracao INSTANCIA = new Configuracao();
    private final String nome = "Sistema Acadêmico";
    private Configuracao() {}
    public static Configuracao getInstancia() { return INSTANCIA; }
    public String getNome() { return nome; }
}

class Aluno {
    private final String nome;
    private final List<Curso> cursos = new ArrayList<>();
    public Aluno(String nome) {
        if (nome == null || nome.isBlank()) throw new IllegalArgumentException("Nome inválido");
        this.nome = nome;
    }
    public String getNome() { return nome; }
    public void adicionarCurso(Curso curso) {
        if (curso == null) throw new IllegalArgumentException("Curso inválido");
        cursos.add(curso);
    }
    public int getQuantidadeCursos() { return cursos.size(); }
}

class Curso {
    private final String titulo;
    private final double preco;
    public Curso(String titulo, double preco) {
        if (titulo == null || titulo.isBlank() || !Double.isFinite(preco) || preco <= 0)
            throw new IllegalArgumentException("Dados inválidos");
        this.titulo = titulo;
        this.preco = preco;
    }
    public String getTitulo() { return titulo; }
    public double getPreco() { return preco; }
}

interface ObservadorMatricula {
    void atualizar(String mensagem);
}

class AvisoEmail implements ObservadorMatricula {
    public void atualizar(String mensagem) { System.out.println("Email:" + mensagem); }
}

class Auditoria implements ObservadorMatricula {
    public void atualizar(String mensagem) { System.out.println("Auditoria:" + mensagem); }
}

interface Comprovante {
    String gerar(Aluno aluno, Curso curso, double valor);
}

class ComprovanteTexto implements Comprovante {
    public String gerar(Aluno aluno, Curso curso, double valor) {
        return String.format("Matrícula: %s / %s / R$ %.2f", aluno.getNome(), curso.getTitulo(), valor);
    }
}

class ComprovanteResumido implements Comprovante {
    public String gerar(Aluno aluno, Curso curso, double valor) {
        return String.format("%s - R$ %.2f", aluno.getNome(), valor);
    }
}

abstract class CriadorComprovante {
    public abstract Comprovante criar();
    public String emitir(Aluno aluno, Curso curso, double valor) {
        return criar().gerar(aluno, curso, valor);
    }
}

class CriadorTexto extends CriadorComprovante {
    @Override public Comprovante criar() { return new ComprovanteTexto(); }
}

class CriadorResumido extends CriadorComprovante {
    @Override public Comprovante criar() { return new ComprovanteResumido(); }
}

class SistemaLegado {
    public void registrarEmCentavos(int centavos) {
        System.out.println("Legado registrou:" + centavos + " centavos");
    }
}

interface RegistradorPagamento {
    void registrar(double reais);
}

class AdaptadorPagamento implements RegistradorPagamento {
    private final SistemaLegado legado;
    public AdaptadorPagamento(SistemaLegado legado) {
        if (legado == null) throw new IllegalArgumentException("Sistema legado inválido");
        this.legado = legado;
    }
    public void registrar(double reais) {
        if (!Double.isFinite(reais) || reais < 0) throw new IllegalArgumentException("Valor inválido");
        legado.registrarEmCentavos((int) Math.round(reais * 100));
    }
}

class ServicoMatricula {
    private final Desconto desconto;
    private final RegistradorPagamento pagamento;
    private final List<ObservadorMatricula> observadores = new ArrayList<>();
    public ServicoMatricula(Desconto desconto, RegistradorPagamento pagamento) {
        if (desconto == null || pagamento == null) throw new IllegalArgumentException("Dependência inválida");
        this.desconto = desconto;
        this.pagamento = pagamento;
    }
    public void inscrever(ObservadorMatricula observador) {
        if (observador == null) throw new IllegalArgumentException("Observador inválido");
        observadores.add(observador);
    }
    public double matricular(Aluno aluno, Curso curso) {
        if (aluno == null || curso == null) throw new IllegalArgumentException("Aluno e curso são obrigatórios");
        double total = desconto.aplicar(curso.getPreco());
        pagamento.registrar(total);
        aluno.adicionarCurso(curso);
        String mensagem = aluno.getNome() + " matriculado em " + curso.getTitulo();
        for (ObservadorMatricula observador : observadores) observador.atualizar(mensagem);
        return total;
    }
}

class FachadaAcademica {
    private final ServicoMatricula servico;
    private final CriadorComprovante criador;
    public FachadaAcademica(ServicoMatricula servico, CriadorComprovante criador) {
        if (servico == null || criador == null) throw new IllegalArgumentException("Dependência inválida");
        this.servico = servico;
        this.criador = criador;
    }
    public String matricularEEmitir(Aluno aluno, Curso curso) {
        double total = servico.matricular(aluno, curso);
        return criador.emitir(aluno, curso, total);
    }
}

public class Main {
    public static void main(String[] args) {
        System.out.println(Configuracao.getInstancia().getNome());
        Aluno aluno = new Aluno("Ana");
        Curso curso = new Curso("Java", 200.00);
        ServicoMatricula servico = new ServicoMatricula(
            new DescontoEstudante(),
            new AdaptadorPagamento(new SistemaLegado())
        );
        servico.inscrever(new AvisoEmail());
        servico.inscrever(new Auditoria());
        FachadaAcademica fachada = new FachadaAcademica(servico, new CriadorTexto());
        System.out.println(fachada.matricularEEmitir(aluno, curso));
        System.out.println("Cursos do aluno: " + aluno.getQuantidadeCursos());
    }
}
