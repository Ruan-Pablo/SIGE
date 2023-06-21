package com.sige.gui.votacao.eventos;

import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.sql.ResultSet;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.sige.gui.recursos.DialogoErro;
import com.sige.gui.votacao.DialogoUrnaEletronica;
import com.sige.persistencia.BancoDadosCandidato;
import com.sige.persistencia.BancoDadosPartido;
import com.sige.persistencia.BancoDadosVotacaoCandidatos;
import com.sige.persistencia.BancoDadosVotacaoCargos;

/**
 * Esta classe implementa um <code>ActionListener</code> e trata os eventos da classe <code>DialogoUrnaEletronica</code>.
 *  
 * @author Charles Garrocho
 * @author Barbara Silveiro
 * 
 * @see DialogoUrnaEletronica
 */
public class TratadorEventosUrnaEletronica implements ActionListener {

	private DialogoUrnaEletronica gui;
	private String BOTOES = getClass().getResource("/sons/botoes.wav").toString();
	private String FIM    = getClass().getResource("/sons/fim.wav").toString();
	private BancoDadosVotacaoCandidatos dataBaseVotacaoCandidatos;
	private BancoDadosVotacaoCargos dataBaseVotacaoCargos;
	private BancoDadosCandidato dataBaseCandidato;
	private BancoDadosPartido dataBasePartido;
	private ResultSet resultado;
	private boolean votou;

	/**
	 * Este e o construtor. Ele recebe como argumento o <code>DialogoUrnaEletronica</code> guarda a referencia dela em uma 
	 * variavel para poder tratar os eventos. O construtor instancia as classes de banco de dados necessarias para poder fazer
	 * as consultas e insercoes no banco de dados.
	 * 
	 * @param dialogoUrnaEletronica um <code>DialogoUrnaEletronica</code>.
	 * 
	 * @see BancoDadosVotacaoCandidatos
	 * @see BancoDadosCandidato
	 * @see BancoDadosPartido
	 * @see BancoDadosVotacaoCargos
	 */
	public TratadorEventosUrnaEletronica(DialogoUrnaEletronica dialogoUrnaEletronica) {
		super();
		this.gui = dialogoUrnaEletronica;
		this.dataBaseVotacaoCandidatos = new BancoDadosVotacaoCandidatos();
		this.dataBaseCandidato = new BancoDadosCandidato();
		this.dataBasePartido = new BancoDadosPartido();
		this.dataBaseVotacaoCargos = new BancoDadosVotacaoCargos();
		this.votou = false;
	}

	/**
	 * Este metodo toca o som da urna eletronica.
	 */
	private void tocaSom() {
		try {
			AudioClip somCliqueConfirma = null;
			if (gui.getQtdeCargos()+1 < gui.getTotalCargos())
				somCliqueConfirma = Applet.newAudioClip(new URL(BOTOES));
			else
				somCliqueConfirma = Applet.newAudioClip(new URL(FIM));
			somCliqueConfirma.play();
		} catch (Exception e) {
			new DialogoErro(gui, "Erro", "Informe o Seguinte Erro ao Analista: " + e.toString());
		}  
	}

	/** 
	 * Este metodo e implementado por que a classe <code>TratadorEventosDialogoUrnaEletronica</code> implementa um 
	 * ActionListener, e por isso ela deve obrigatoriamente implementar os metodos abstratados dessa classe. Este metodo 
	 * trata os eventos de todos os botoes da urna eletronica.
	 */
	public void actionPerformed(ActionEvent evento) {
		if (votou) {
			return;
		}

		if (evento.getSource() == gui.getBotaoBranco()) {
			tratarBotaoBranco();
		} else if (evento.getSource() == gui.getBotaoCorrige()) {
			corrigeTela();
		} else if (evento.getSource() == gui.getBotaoConfirma()) {
			tratarBotaoConfirma();
		} else {
			tratarBotoesNumericos(evento.getSource());
		}
	}

	private void tratarBotaoBranco() {
		if (gui.getNumeroCandidato().getText().trim().length() == 0) {
			exibirPainelVotoBranco();
		}
	}

	private void exibirPainelVotoBranco() {
		gui.getSeuVotoParaLabel().setVisible(true);
		gui.getAperte().setVisible(true);
		gui.getLabelLinha().setVisible(true);
		gui.getLabelLinha2().setVisible(true);
		gui.getLaranjaCorrige().setVisible(true);
		gui.getVerdeConfirma().setVisible(true);
		gui.getNumeroLabel().setText("  VOTO EM");
		gui.getNumeroLabel().setFont(new Font(null, Font.PLAIN, 31));
		gui.getNumeroCandidato().setText("BRANCO");
		gui.getNumeroCandidato().setBorder(BorderFactory.createEmptyBorder());
		gui.getNumeroCandidato().setFont(new Font(null, Font.PLAIN, 31));
		gui.setCaminhoFoto(gui.TRANSPARENTE);
	}

	private void tratarBotaoConfirma() {
		if (gui.getTotalCargos() != gui.getQtdeCargos()) {
			try {
				if (!gui.getCaminhoFoto().equalsIgnoreCase(gui.TRANSPARENTE)) {
					realizarVotoCandidato();
				} else if (gui.getPartidoCandidato().getText().equalsIgnoreCase("VOTO NULO")) {
					realizarVotoNulo();
				} else if (gui.getNumeroCandidato().getText().equalsIgnoreCase("BRANCO")) {
					realizarVotoBranco();
				}
			} catch (Exception e) {
				exibirErroAnalista(e);
			}
		}

		if (gui.getTotalCargos() == gui.getQtdeCargos()) {
			exibirPainelFimVotacao();
		}
	}

	private void realizarVotoCandidato() throws Exception {
		tocaSom();

		dataBaseVotacaoCandidatos.iniciaConexao();
		int votos = dataBaseVotacaoCandidatos.obterVotosCandidato(gui.getNomeCandidato().getText(), gui.getDATA_VOTACAO());

		if (votos == 0)
			dataBaseVotacaoCandidatos.adicionarVotacaoCandidatos(gui.getDATA_VOTACAO(), gui.getCargos()[gui.getQtdeCargos()], gui.getNomeCandidato().getText(), votos + 1);
		else
			dataBaseVotacaoCandidatos.atualizarVotacaoCandidatos(gui.getNomeCandidato().getText(), votos + 1);
		dataBaseVotacaoCandidatos.fechaConexao();

		dataBaseVotacaoCargos.iniciaConexao();
		dataBaseVotacaoCargos.atualizarVotacaoCargos(gui.getDATA_VOTACAO(), gui.getCargos()[gui.getQtdeCargos()], gui.getNomeCandidato().getText(), gui.getQtdeCargos() + 1);
		dataBaseVotacaoCargos.fechaConexao();

		proximoCargo();
	}

	private void realizarVotoNulo() throws Exception {
		tocaSom();

		dataBaseVotacaoCargos.iniciaConexao();
		dataBaseVotacaoCargos.atualizarVotacaoCargos(gui.getDATA_VOTACAO(), gui.getCargos()[gui.getQtdeCargos()], "VOTO NULO", gui.getQtdeCargos() + 1);
		dataBaseVotacaoCargos.fechaConexao();

		proximoCargo();
	}

	private void realizarVotoBranco() throws Exception {
		tocaSom();

		dataBaseVotacaoCargos.iniciaConexao();
		dataBaseVotacaoCargos.atualizarVotacaoCargos(gui.getDATA_VOTACAO(), gui.getCargos()[gui.getQtdeCargos()], "BRANCO", gui.getQtdeCargos() + 1);
		dataBaseVotacaoCargos.fechaConexao();

		proximoCargo();
	}

	private void exibirErroAnalista(Exception e) {
		JOptionPane.showMessageDialog(gui, "Erro ao votar: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
	}

	private void exibirPainelFimVotacao() {
		gui.getNumeroLabel().setVisible(false);
		gui.getNumeroCandidato().setVisible(false);
		gui.getNomeCandidato().setVisible(false);
		gui.getPartidoCandidato().setVisible(false);
		gui.getFotoCandidato().setVisible(false);
		gui.getLabelCargos().setVisible(false);
		gui.getLabelCargosVotados().setVisible(false);
		gui.getLabelFim().setVisible(true);
		gui.getLabelCargos().setText("VOTAÇÃO ENCERRADA");
		gui.getBotaoBranco().setEnabled(false);
		gui.getBotaoCorrige().setEnabled(false);
		gui.getBotaoConfirma().setEnabled(false);
		gui.getBotaoSair().setVisible(true);
	}

	private void proximoCargo() {
		gui.zerarControlesTela();

		if (gui.getQtdeCargos() < gui.getTotalCargos() - 1) {
			gui.setQtdeCargos(gui.getQtdeCargos() + 1);
			exibirPainelCargos();
		} else {
			exibirPainelFimVotacao();
		}
	}

	private void tratarBotoesNumericos(Object source) {
		JButton button = (JButton) source;
		String buttonText = button.getText();

		if (gui.getNumeroCandidato().getText().length() < 5) {
			if (buttonText.equals("0") && gui.getNumeroCandidato().getText().equals("0")) {
				return;
			}

			gui.getNumeroCandidato().setText(gui.getNumeroCandidato().getText() + buttonText);
		}
	}


	/**
	 * Este metodo corrige a visualizacao do painel.
	 */
	private void corrigeTela() {
		gui.atualizaCargoLabel();
		gui.getNumeroCandidato().setFont(new Font(null, Font.BOLD, 29));
		gui.getNumeroCandidato().setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
		gui.getNumeroLabel().setText("  Numero: ");
		gui.getNumeroCandidato().setText(" ");
		gui.getNomeCandidato().setText("");
		gui.getPartidoCandidato().setText("");
		gui.getNumeroLabel().setFont(new Font(null, Font.PLAIN, 22));
		gui.getNomeLabel().setVisible(false);
		gui.getPartidoLabel().setVisible(false);
		gui.getSeuVotoParaLabel().setVisible(false);
		gui.getAperte().setVisible(false);
		gui.getLabelLinha().setVisible(false);
		gui.getLabelLinha2().setVisible(false);
		gui.getLaranjaCorrige().setVisible(false);
		gui.getVerdeConfirma().setVisible(false);
		gui.addFotoCandidato(gui.TRANSPARENTE);
	}

	/**
	 * Este metodo trata o botao que e passado como argumento.
	 * 
	 * @param botao um <code>int</code> com o numero do botao que sera tratado.
	 */
	private void trataBotoes(int botao) {
		gui.getNumeroCandidato().setText(gui.getNumeroCandidato().getText().trim());

		// Verifica se a quantidade de digitos do labelNumero e menor que a quantidade total de digitos do cargo atual.
		if (gui.getNumeroCandidato().getText().length() < gui.getDigitos()[gui.getQtdeCargos()]) {
			gui.getNumeroCandidato().setText(gui.getNumeroCandidato().getText() + botao);
		}

		/* verifica se a quantidade de digitos do labelNumero e igual a quantidade total de digitos do cargo atual. Se sim
		   o metodo pesquisaCandidato e chamado. */
		if (gui.getNumeroCandidato().getText().length() == gui.getDigitos()[gui.getQtdeCargos()])
			pesquisaCandidato();

		// Verifica se o usuario clicou no botao Branco. Se nao e feito uma busca de partido no banco de dados.
		if (!gui.getNumeroCandidato().getText().equalsIgnoreCase("BRANCO") ) {
			pesquisaPartido();
		}
	}

	/**
	 * Este metodo busca no banco de dados um partido a partir dos numeros digitos na urna eletronica.
	 */
	private void pesquisaPartido() {
		try {
			int verifica = 0;

			// Verifica se a quantidade de digitos e menor ou igual a quantidade total de digitos daquele partido.
			if (gui.getNumeroCandidato().getText().length() <= gui.getDigitos()[gui.getQtdeCargos()]) {
				dataBasePartido.iniciaConexao();

				// Verifica se existe um partido com este numero.
				if ( dataBasePartido.verificaPartido(Integer.parseInt(gui.getNumeroCandidato().getText())) != 0) {
					dataBasePartido.fechaConexao();

					dataBaseCandidato.iniciaConexao();
					verifica = dataBaseCandidato.verificaCandidato(Integer.parseInt(gui.getNumeroCandidato().getText()));
					dataBaseCandidato.fechaConexao();

					// Se existir um partido com este numero, e adicionado a sigla do partido no labelPartido.
					if ( verifica != 0 ) {
						dataBasePartido.iniciaConexao();
						resultado = dataBasePartido.obterPartido(Integer.parseInt(gui.getNumeroCandidato().getText()));
						while(resultado.next()) {
							gui.getPartidoCandidato().setText(resultado.getString("sigla"));
						}
						gui.getPartidoLabel().setVisible(true);
						gui.getPartidoCandidato().setFont(new Font(null, Font.BOLD, 18));
						dataBasePartido.fechaConexao();
					}
				}
				else
					dataBasePartido.fechaConexao();
			}
			dataBaseCandidato.iniciaConexao();
			verifica = dataBaseCandidato.verificaCandidatoPorCargo(Integer.parseInt(gui.getNumeroCandidato().getText()), gui.getCargos()[gui.getQtdeCargos()] );
			dataBaseCandidato.fechaConexao();

			/* Caso a quantidade de digitos tenha chegado ao total de digitos daquele cargo e nao tennha sido encontrado nenhum
			   partido para aquele numero uma mensagem de numero errado e Nulo e enviado a tela.*/
			if (gui.getNumeroCandidato().getText().length() == gui.getDigitos()[gui.getQtdeCargos()] && verifica == 0) {
				gui.getNomeLabel().setText("  NUMERO ERR");
				gui.getNomeCandidato().setText("ADO");
				gui.getNomeCandidato().setFont(new Font(null, Font.PLAIN, 20));
				gui.getPartidoCandidato().setText("VOTO NULO");
				gui.getPartidoCandidato().setFont(new Font(null, Font.PLAIN, 28));

				gui.getPartidoLabel().setVisible(false);
				gui.getNomeLabel().setVisible(true);
				gui.getSeuVotoParaLabel().setVisible(true);
				gui.getAperte().setVisible(true);
				gui.getLabelLinha().setVisible(true);
				gui.getLabelLinha2().setVisible(true);
				gui.getLaranjaCorrige().setVisible(true);
				gui.getVerdeConfirma().setVisible(true);
			}

		} catch (Exception e) {
			new DialogoErro(gui, "Erro", "Informe o Seguinte Erro ao Analista: " + e.toString());
		}
	}

	/**
	 * Este metodo pesquisa um candidato a partir dos numeros fornecidos na urna eletronica.
	 */
	public void pesquisaCandidato() {

		try {
			dataBaseCandidato.iniciaConexao();

			// Verifica se existe um candidato com este numero.
			if (dataBaseCandidato.verificaCandidatoPorCargo(Integer.parseInt(gui.getNumeroCandidato().getText()), gui.getCargos()[gui.getQtdeCargos()]) != 0) {
				resultado = dataBaseCandidato.obterCandidatoPorCargo(Integer.parseInt(gui.getNumeroCandidato().getText()), gui.getCargos()[gui.getQtdeCargos()]);

				// Adiciona os dados do candidatos na tela.
				while(resultado.next()) {
					gui.getNomeCandidato().setText(resultado.getString("nome"));
					gui.addFotoCandidato(resultado.getString("caminhoFoto"));
				}

				// Verifica se existe espaco no nome do candidato. Caso haja copia apenas o primeiro nome do candidato.
				if (gui.getNomeCandidato().getText().indexOf(" ") != -1)
					gui.getNomeCandidato().setText(gui.getNomeCandidato().getText().substring(0, gui.getNomeCandidato().getText().indexOf(" ")));
				else
					// Caso nao haja espacos, copia todo o nome do candidato.
					gui.getNomeCandidato().setText(gui.getNomeCandidato().getText());
				gui.getNomeCandidato().setFont(new Font(null, Font.BOLD, 18));
				gui.getNomeLabel().setText("  Nome:");
				gui.getNomeLabel().setVisible(true);
				gui.getPartidoLabel().setVisible(true);
				gui.getSeuVotoParaLabel().setVisible(true);
				gui.getAperte().setVisible(true);
				gui.getLabelLinha().setVisible(true);
				gui.getLabelLinha2().setVisible(true);
				gui.getLaranjaCorrige().setVisible(true);
				gui.getVerdeConfirma().setVisible(true);
				gui.getNumeroCandidato().setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
				dataBaseCandidato.fechaConexao();
			}
			else
				dataBaseCandidato.fechaConexao();
		} catch (Exception e) {
			new DialogoErro(gui, "Erro", "Informe o Seguinte Erro ao Analista: " + e.toString());
		}
	}
}
