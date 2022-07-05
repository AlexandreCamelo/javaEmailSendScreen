package br.com.tudodev.envioemails.bean;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Scope("viewJSF")
@Named
@Data
@AllArgsConstructor
@NoArgsConstructor
public class inicioBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Autowired
	EmailBean emailBean;

	List<String> emailsTo = new ArrayList<>();
	List<String> emailsCC = new ArrayList<>();
	List<String> emailsCCO = new ArrayList<>();
	List<File> attachs = new ArrayList<>();

	@PostConstruct
	private void init() {
		emailsTo.add("johnmacclane@gmail.com");
		emailsTo.add("hollygennero@gmail.com");

		emailsCC.add("martinriggs@gmail.com");
		emailsCC.add("lornacole@gmail.com");

		emailsCCO.add("rogermurtaugh@gmail.com");
		emailsCCO.add("leogetz@gmail.com");

		File emailFile = new File(Paths.get("src/main/resources/static/imagens/clipe.png").toAbsolutePath().toString());
		attachs.add(emailFile);

	}

	public void openEmailScreen() {
		emailBean.to(emailsTo);
		emailBean.cc(emailsCC);
		emailBean.cco(emailsCCO);
		emailBean.subject("The nakatomi is ours...");
		emailBean.attachments(attachs);
		emailBean.body("<h3>Dear McLane,</h3><br/><br/><p>We are coming to nakatomi.</p>");
		emailBean.showEmail();
	}

}