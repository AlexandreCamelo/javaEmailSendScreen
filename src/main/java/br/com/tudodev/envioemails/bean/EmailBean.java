package br.com.tudodev.envioemails.bean;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.MimeMessage;

import org.apache.commons.io.FileUtils;
import org.primefaces.PrimeFaces;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFiles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.FileSystemResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import br.com.tudodev.envioemails.modelos.EmailAutoComplete;
import br.com.tudodev.envioemails.modelos.EmailEnviado;
import br.com.tudodev.envioemails.modelos.EmailNomeEArquivo;
import br.com.tudodev.envioemails.repositorios.EmailAutoCompleteRepository;
import br.com.tudodev.envioemails.repositorios.EmailEnviadoRepository;
import br.com.tudodev.envioemails.utilidades.IdUnicoApp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Component
@Scope("viewJSF")
@Named
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Value("${envioemails.attachmentdirectory}")
	private String diretorioAnexos;

	@Value("${envioemails.presentationname}")
	private String emailNomeApresentacao;

	@Value("${envioemails.pathlogo}")
	private String caminhoLogo;

	@Value("${envioemails.emailsender}")
	private String emailDe;

	@Value("${envioemails.nomeassinatura}")
	private String emailAssinatNome;

	@Value("${envioemails.cargoassinatura}")
	private String emailAssinatCargo;

	@Value("${envioemails.foneassinatura}")
	private String emailAssinatFone;

	@Value("${envioemails.website}")
	private String emailAssinatSite;

	@Autowired
	EmailEnviadoRepository repEmailEnviado;

	@Autowired
	EmailAutoCompleteRepository repEmailAutoC;

	@Autowired
	JavaMailSender enviador;

	MimeMessage email;
	MimeMessageHelper mensagem;

	private String strEditor;
	private String emailCc;
	private String emailCco;
	private String emailAssunto;
	private String arquivosAnexadosParaBD;
	private List<String> listaAutoCompleteEmails = new ArrayList<>();;
	private List<String> listaAutoCompleteEmailsSelecionados = new ArrayList<>();
	private List<String> listaAutoCompleteEmailsSelecionadosCC = new ArrayList<>();
	private List<String> listaAutoCompleteEmailsSelecionadosCCO = new ArrayList<>();
	private List<File> listaAutoCompleteEmailsSelecionadosAnexos = new ArrayList<>();
	File PDFDoOrcamento;
	private UploadedFiles arquivosEnviados;

	List<EmailNomeEArquivo> arquivosAnexados = new ArrayList<>();
	List<File> arquivosAnexadosParaTentativaDeApagamento = new ArrayList<>();
	EmailEnviado salvarEmailEnviado = new EmailEnviado();

	@PostConstruct
	public void Iniciar() {

		System.out.println("ANEXOS: " + diretorioAnexos);
		System.out.println("LOGO: " + caminhoLogo);
		System.out.println("APRESENTAÇÃO: " + emailNomeApresentacao);

		criarPastaArquivos(diretorioAnexos);
		strEditor = "";
		emailAssunto = "";
		preencherAutoCompleteEmail();

	}

	public void to(List<String> emails) {
		listaAutoCompleteEmailsSelecionados = emails;
	}

	public void cc(List<String> emails) {
		listaAutoCompleteEmailsSelecionadosCC = emails;
	}

	public void cco(List<String> emails) {
		listaAutoCompleteEmailsSelecionadosCCO = emails;
	}

	public void subject(String subject) {
		emailAssunto = subject;
	}

	public void attachments(List<File> files) {
		List<EmailNomeEArquivo> attachs = new ArrayList<>();

		for (File f : files) {
			EmailNomeEArquivo em = new EmailNomeEArquivo();
			em.setId(IdUnicoApp.gerarIdUUID());
			em.setNome(f.getName());
			em.setArquivo(f);
			attachs.add(em);
		}

		arquivosAnexados = attachs;
	}

	public void body(String body) {
		strEditor = body;
	}

	public void body(File file) {
		strEditor = lerArquivo(file);
	}

	public void showEmail() {
		PrimeFaces.current().executeScript("PF('dlgEmailGenerico').show();");
		PrimeFaces.current().ajax().update("formEmail:txtPara");
		PrimeFaces.current().ajax().update("formEmail:txtCc");
		PrimeFaces.current().ajax().update("formEmail:txtCco");
		PrimeFaces.current().ajax().update("formEmail:txtAssunto");
		PrimeFaces.current().ajax().update("formEmail:txtAnexos");
		PrimeFaces.current().ajax().update("formEmail:editor");
	}

	private void salvarEmailNoBD() {
		try {
			salvarEmailEnviado.setRemetente(emailDe);
			salvarEmailEnviado.setPara(
					listaAutoCompleteEmailsSelecionados.toString().replaceAll("[\\[\\]]", "").replaceAll(",", ";"));

			salvarEmailEnviado.setCc(
					listaAutoCompleteEmailsSelecionadosCC.toString().replaceAll("[\\[\\]]", "").replaceAll(",", ";"));

			salvarEmailEnviado.setCco(
					listaAutoCompleteEmailsSelecionadosCCO.toString().replaceAll("[\\[\\]]", "").replaceAll(",", ";"));

			salvarEmailEnviado.setAssunto(emailAssunto);
			salvarEmailEnviado.setCorpo(strEditor);
			salvarEmailEnviado.setListaDeAnexos(arquivosAnexadosParaBD);
			salvarEmailEnviado.setDataHora(LocalDateTime.now());
			repEmailEnviado.save(salvarEmailEnviado);
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_WARN, "ENVIADO COM RESTRIÇÕES",
							"O email foi enviado, mas não ficou salvo no sistema (motivo desconhecido)."));
			PrimeFaces.current().ajax().update("formEmail:grownEmail");
			e.printStackTrace();
		}
	}

	public void prepararEmailComAnexo() {
		if (listaAutoCompleteEmailsSelecionados == null || listaAutoCompleteEmailsSelecionados.size() == 0) {
			if (listaAutoCompleteEmailsSelecionadosCC == null || listaAutoCompleteEmailsSelecionadosCC.size() == 0) {
				if (listaAutoCompleteEmailsSelecionadosCCO == null
						|| listaAutoCompleteEmailsSelecionadosCCO.size() == 0) {
					FacesContext.getCurrentInstance().addMessage(null,
							new FacesMessage(FacesMessage.SEVERITY_WARN, "SEM EMAIL", "Insira ao menos 1 email."));
					return;
				}
			}
		}
		try {
			System.out.println("Começando a enviar...");
			Path novoArquivo = Paths.get("src/main/resources/static/imagens/logo.jpg").toAbsolutePath();
			email = enviador.createMimeMessage();
			mensagem = new MimeMessageHelper(email, true);
			File logo = new File(novoArquivo.toAbsolutePath().toString());
			mensagem.setFrom(emailDe, emailNomeApresentacao);

			if (listaAutoCompleteEmailsSelecionados != null || listaAutoCompleteEmailsSelecionados.size() > 0) {
				mensagem.setTo(listaAutoCompleteEmailsSelecionados.toArray(new String[0]));
			}

			if (listaAutoCompleteEmailsSelecionadosCC != null || listaAutoCompleteEmailsSelecionadosCC.size() > 0) {
				mensagem.setCc(listaAutoCompleteEmailsSelecionadosCC.toArray(new String[0]));
			}

			if (listaAutoCompleteEmailsSelecionadosCCO != null && listaAutoCompleteEmailsSelecionadosCCO.size() > 0) {
				List<String> emailsOcultos = new ArrayList<>();
				emailsOcultos = listaAutoCompleteEmailsSelecionadosCCO;
				emailsOcultos.add(emailDe);
				mensagem.setBcc(emailsOcultos.toArray(new String[0]));
			} else {
				mensagem.setBcc(emailDe);
			}

			mensagem.setSubject(emailAssunto);
			strEditor = strEditor + htmlAssinaturaEmail();
			mensagem.setText(strEditor, true);
			FileSystemResource logoEmbutido = new FileSystemResource(logo);
			mensagem.addInline("cidDoLogo", logoEmbutido);

			for (EmailNomeEArquivo arq : arquivosAnexados) {
				mensagem.addAttachment(arq.getNome(), arq.getArquivo());
				arquivosAnexadosParaBD = arquivosAnexadosParaBD + ";" + arq.getNome();

			}
		} catch (AddressException ae) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"EMAIL INVÁLIDO", "Algum email é inválido. Verifique a digitação."));
			limparDadosEmail();
			ae.printStackTrace();

		} catch (MessagingException e) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"PROBLEMAS", "Ocorreu algum problema desconhecido. Não foi possível enviar o email."));
			limparDadosEmail();
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"PROBLEMAS", "Ocorreu algum problema desconhecido. Não foi possível enviar o email."));
			limparDadosEmail();
			e.printStackTrace();
		} catch (Exception ex) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"PROBLEMAS", "Ocorreu algum problema desconhecido. Não foi possível enviar o email."));
			limparDadosEmail();
			ex.printStackTrace();
		}

	}

	public void sohEnviar() {
		prepararEmailComAnexo();
		System.out.println("Vou enviar agora...");
		enviador.send(email);
		System.out.println("Enviado!!!");
		salvarEmailNoBD();
		limparDadosEmail();
		FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "OK.", "Enviado.");
		PrimeFaces.current().dialog().showMessageDynamic(message);
	}

	public void uploadArquivo(FileUploadEvent event) {
		try {
			File enviado = criarArquivoComNomeUnico(diretorioAnexos, "", event.getFile().getFileName());
			EmailNomeEArquivo nomeEArquivo = new EmailNomeEArquivo(IdUnicoApp.gerarIdUUID(),
					event.getFile().getFileName(), enviado);

			if (enviado == null || enviado.getName().length() <= 0) {
				System.out.println("'enviado' é nulo!!!!");
				return;
			}

			FileUtils.copyInputStreamToFile(event.getFile().getInputStream(), enviado);
			arquivosAnexados.add(nomeEArquivo);
			arquivosAnexadosParaTentativaDeApagamento.add(enviado);
			System.out.println(arquivosAnexados);
		} catch (IOException ie) {
			ie.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private boolean criarPastaArquivos(String caminho) {
		try {
			String novaPasta = caminho;
			File pasta = new File(novaPasta);

			if (pasta.exists()) {
				return true;
			}

			pasta.mkdir();
			pasta.setWritable(true);
			pasta.setExecutable(true);
			pasta.deleteOnExit();
			return true;

		} catch (Exception e) {
			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "PROBLEMAS.",
					"Ocorreu um problema desconhecido. Limpe o cache, reinicie o sistema e tente novamente.");
			PrimeFaces.current().dialog().showMessageDynamic(message);
			e.printStackTrace();
			return false;
		}
	}

	private File criarArquivoComNomeUnico(String pastaOndeColocarOArquivo, String prefixo,
			String nomeArquivoComExtensao) {
		String strPrefixo = "";
		String strArquivoComExtensao = "";

		if (prefixo != null && !prefixo.isBlank()) {
			strPrefixo = prefixo;
		}

		if (nomeArquivoComExtensao != null && !nomeArquivoComExtensao.isBlank()) {
			strArquivoComExtensao = nomeArquivoComExtensao;
		}

		try {
			Path novoArquivo = Paths.get(pastaOndeColocarOArquivo)
					.resolve(strPrefixo + IdUnicoApp.gerarIdUUID() + strArquivoComExtensao);
			File arquivo = new File(novoArquivo.toString());

			if (arquivo.exists()) {
				do {
					System.out.println("O arquivo " + arquivo.getName() + " já existe. Tentando criar outro.");
					novoArquivo = Paths.get(pastaOndeColocarOArquivo)
							.resolve(strPrefixo + IdUnicoApp.gerarIdUUID() + strArquivoComExtensao);
					arquivo = new File(novoArquivo.toString());
				} while (arquivo.exists());
			}

			arquivo.setWritable(true);
			arquivo.setExecutable(true);
			arquivo.deleteOnExit();
			return arquivo;

		} catch (Exception e) {
			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "PROBLEMAS.",
					"Ocorreu um problema desconhecido. Limpe o cache, reinicie o sistema e tente novamente.");
			PrimeFaces.current().dialog().showMessageDynamic(message);
			e.printStackTrace();
			return new File("");
		}
	}

	public void limparDadosEmail() {
		try {
			PDFDoOrcamento = new File("");
			strEditor = "";
			emailDe = "";
			emailAssunto = "";
			emailCc = "";
			emailCco = "";
			email = enviador.createMimeMessage();
			mensagem = new MimeMessageHelper(email, true);
			arquivosAnexados = new ArrayList<>();
			listaAutoCompleteEmailsSelecionados = new ArrayList<>();
			listaAutoCompleteEmailsSelecionadosCC = new ArrayList<>();
			listaAutoCompleteEmailsSelecionadosCCO = new ArrayList<>();

			for (File arq : arquivosAnexadosParaTentativaDeApagamento) {
				if (arq.exists()) {
					arq.delete();
				}
			}
			PDFDoOrcamento.delete();
			arquivosAnexadosParaTentativaDeApagamento = new ArrayList<>();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}

	public void incluirNovoEmailNoAutocomplete(String tipo) {
		try {
			String emailAIncluir = "";
			if (tipo.equals("para")) {
				System.out.println("EMAIL PARA");
				int tamanho = listaAutoCompleteEmailsSelecionados.size();
				emailAIncluir = listaAutoCompleteEmailsSelecionados.get(tamanho - 1);

				if (!ehEmail(emailAIncluir)) {
					System.out.println("EMAIL INVÁLIDO");
					Integer indice = listaAutoCompleteEmailsSelecionados.size() - 1;
					listaAutoCompleteEmailsSelecionados.remove(listaAutoCompleteEmailsSelecionados.get(indice));
				}
			} else if (tipo.equals("cc")) {
				System.out.println("EMAIL CC");
				int tamanho = listaAutoCompleteEmailsSelecionadosCC.size();
				emailAIncluir = listaAutoCompleteEmailsSelecionadosCC.get(tamanho - 1);

				if (!ehEmail(emailAIncluir)) {
					System.out.println("EMAIL INVÁLIDO");
					Integer indice = listaAutoCompleteEmailsSelecionadosCC.size() - 1;
					listaAutoCompleteEmailsSelecionadosCC.remove(listaAutoCompleteEmailsSelecionadosCC.get(indice));
				}
			} else if (tipo.equals("cco")) {
				System.out.println("EMAIL CCO");
				int tamanho = listaAutoCompleteEmailsSelecionadosCCO.size();
				emailAIncluir = listaAutoCompleteEmailsSelecionadosCCO.get(tamanho - 1);

				if (!ehEmail(emailAIncluir)) {
					System.out.println("EMAIL INVÁLIDO");
					Integer indice = listaAutoCompleteEmailsSelecionadosCCO.size() - 1;
					listaAutoCompleteEmailsSelecionadosCCO.remove(listaAutoCompleteEmailsSelecionadosCCO.get(indice));
				}
			}

			EmailAutoComplete auto = new EmailAutoComplete();
			auto.setEmail(emailAIncluir.toLowerCase());
			repEmailAutoC.save(auto);
			listaAutoCompleteEmails.add(emailAIncluir);

		} catch (DataIntegrityViolationException data) {
			String causa = data.getMostSpecificCause().toString().toLowerCase();
			if (causa.contains("duplicate key") || causa.contains("already exists")
					|| causa.contains("unique constraint")) {

				System.out.println("Email já existente. Não adicionado.");
			} else {
				System.out.println("O Email não foi adicionado, mas por alguma causa desconhecida.");
			}
		} catch (Exception e) {
			System.out.println("O Email não foi adicionado, mas por alguma causa desconhecida (EXCEPTION GERAL).");

		}
	}

	private boolean ehEmail(String email) {
		String regx = "^(.+)@(.+)$";
		Pattern pattern = Pattern.compile(regx);
		Matcher matcher = pattern.matcher(email);
		return matcher.matches();
	}

	public List<String> retornaAutoCompleteEmail(String digitacao) {
		String digitacaoLowerCase = digitacao.toLowerCase();
		return listaAutoCompleteEmails.stream().filter(t -> t.toLowerCase().startsWith(digitacaoLowerCase))
				.collect(Collectors.toList());
	}


	private void preencherAutoCompleteEmail() {

		List<EmailAutoComplete> listaDoBancoDeDados = repEmailAutoC.findAll();
		listaAutoCompleteEmails = new ArrayList<>();

		for (EmailAutoComplete em : listaDoBancoDeDados) {
			listaAutoCompleteEmails.add(em.getEmail());
		}
	}

	public String htmlAssinaturaEmail() {
		String mensagem = "    <table>\n" + "        <tbody>\n" + "            <tr>\n"
				+ "                <td style=\"width: 155px;\" colspan=\"2\" rowspan=\"5\">\n"
				+ "                    <p>&nbsp;</p>\n"
				+ "                    <img src=\"cid:cidDoLogo\" alt=\"\" width=\"auto\" height=\"150\" />\n"
				+ "                </td>\n" + "\n" + "                <td>\n"
				+ "                    <tr style=\"height: 30px; padding: 0px;\">\n"
				+ "                        <td style=\"width: 300px;\">\n"
				+ "                            <p style=\"height: 10px; color: #808080; font-size: 25px; margin: 0px 0px 10px 0px;\">\n"
				+ "                                <strong>" + emailAssinatNome + "</strong>\n"
				+ "                            </p>\n" + "                        </td>\n"
				+ "                    </tr>\n" + "                    <tr style=\"height: 30px; padding: 0px;\">\n"
				+ "                        <td style=\"width: 300px;\">\n"
				+ "                            <p style=\"height: 10px; color: #808080; font-size: 15px; margin: 0px 0px 10px 0px;\">\n"
				+ "                                <strong>" + emailAssinatCargo + "</strong>\n"
				+ "                            </p>\n" + "                        </td>\n"
				+ "                    </tr>\n" + "                    <tr style=\"height: 30px; padding: 0px;\">\n"
				+ "                        <td style=\"width: 300px;\">\n" + "\n"
				+ "                            <p style=\"height: 10px; color: #808080; font-size: 25px; margin: 0px 0px 10px 0px;\">\n"
				+ "                                <strong>" + emailAssinatFone + "</strong>\n"
				+ "                            </p>\n" + "\n" + "                        </td>\n"
				+ "                    </tr>\n" + "                    <tr style=\"height: 30px; padding: 0px;\">\n"
				+ "                        <td style=\"width: 300px;\">\n"
				+ "                            <a href=\"https://" + emailAssinatSite + "\" target=\"_blank\"\n"
				+ "                                style=\"color: #E57308; font-size: 20px;\"><strong>"
				+ emailAssinatSite + "</strong></p>\n" + "                        </td>\n"
				+ "                    </tr>\n" + "                </td>\n" + "            </tr>\n"
				+ "        </tbody>\n" + "    </table>\n" + "";

		return mensagem;
	}

	public void evitarEnter() {
		System.out.println("Um enter foi evitado");
	}

	private String lerArquivo(File arquivo) {
		File email = arquivo;
		try {
			try (FileReader fr = new FileReader(email)) {
				String retorno = "";

				int i;
				while ((i = fr.read()) != -1) {
					retorno = retorno + (char) i;
				}

				return retorno;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return "";
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}

	}

}