package br.edu.utfpr.rnc.managedbean.rnc;

import br.edu.utfpr.rnc.dao.departamento.DepartamentoDao;
import br.edu.utfpr.rnc.dao.rnc.RncDao;
import br.edu.utfpr.rnc.dao.usuario.UsuarioDao;
import br.edu.utfpr.rnc.managedbean.GerenciadorPaginas;
import br.edu.utfpr.rnc.pojo.rnc.Rnc;
import br.edu.utfpr.rnc.pojo.usuario.Usuario;
import br.edu.utfpr.rnc.util.JsfUtil;
import java.io.*;
import java.util.*;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.swing.ImageIcon;
import net.sf.jasperreports.engine.*;

@ManagedBean(name = "rncBean")
@SessionScoped
public class RncBean {

    @EJB
    private DepartamentoDao departamentoDao;
    @EJB
    private UsuarioDao usuarioDao;
    @EJB
    private RncDao rncDao;
    private Rnc rnc;
    private List<String> origensRnc;
    private List<String> disposicoesRnc;
    private Rnc cabRNC;

    public RncBean() {
        this.rnc = new Rnc();
        this.origensRnc = new ArrayList<String>(Arrays.asList("Reclamação de Cliente",
                "Auditoria de Qualidade",
                "Interna",
                "Fornecedor",
                "Metas",
                "Melhorias",
                "Pesquisa de Satisfação"));
        this.disposicoesRnc = new ArrayList<String>(Arrays.asList("Aceito com Desvio",
                "Sucatar",
                "Devolver",
                "Retrabalho Interno",
                "Retrabalho Externo"));
    }

    public List<String> getOrigensRnc() {
        if (rnc.getOrigemRnc() != null && !rnc.getOrigemRnc().isEmpty()) {
            if (!origensRnc.contains(rnc.getOrigemRnc())) {
                origensRnc.add(rnc.getOrigemRnc());
            }
        }
        return origensRnc;
    }

    public List<String> getDisposicoesRnc() {
        if (rnc.getDisposicao() != null && !rnc.getDisposicao().isEmpty()) {
            if (!disposicoesRnc.contains(rnc.getDisposicao())) {
                disposicoesRnc.add(rnc.getDisposicao());
            }
        }
        return disposicoesRnc;
    }

    private void gerarNovaRnc() {
        Rnc novaRnc = new Rnc(rnc);
        novaRnc.setDataRnc(new Date());
        rncDao.criarEntidade(novaRnc);
        this.rnc.setNovaRnc(novaRnc);
        rnc.setFinalizado(true);
        rncDao.editar(this.rnc);
        this.rnc = novaRnc;
        ((GerenciadorPaginas) JsfUtil.getObjectFromSession("gerenciadorPaginas")).cadastroRNC();
    }

    private void finalizarRnc() {
        rnc.setFinalizado(true);
        rncDao.editar(rnc);
    }

    private java.awt.Image getLogotipoImage() {
        try {
            byte[] bytes = JsfUtil.bytesFromFile(new File(getClass().getResource("../../../../../../../../relatorios/rnc/logo.png").getFile()));
            return new ImageIcon(bytes).getImage();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @FacesConverter(forClass = Rnc.class)
    public static class RncConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0 || value.equals("null")) {
                return null;
            }
            RncBean rncBean = (RncBean) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "rncBean");
            return rncBean.rncDao.buscar(getKey(value));
        }

        java.lang.Integer getKey(String value) {
            java.lang.Integer key;
            key = Integer.valueOf(value);
            return key;
        }

        String getStringKey(java.lang.Integer value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof Rnc) {
                Rnc o = (Rnc) object;
                return getStringKey(o.getId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + Rnc.class.getName());
            }
        }
    }

    public RncBean.RncConverter getConverter() {
        return new RncBean.RncConverter();
    }

    public Rnc getRnc() {
        return rnc;
    }

    public void setRnc(Rnc rnc) {
        this.rnc = rnc;
    }

    public void aprovar() {
        Rnc rnc = (Rnc) JsfUtil.getObjectFromSession("rnc");
        this.rnc = rnc;
        this.rnc.setAprovado(true);
        this.salvar();

    }

    public void salvar() {
        try {
            System.out.println(rnc.getId());
            if (rnc.getId() == 0) {
                rncDao.criarEntidade(rnc);
            } else {
                rncDao.editar(rnc);
                ((GerenciadorPaginas) JsfUtil.getObjectFromSession("gerenciadorPaginas")).listarRNCs();
            }
            if (rnc.getEficaz() != null) {
                if (rnc.getEficaz().equals("Não")) {
                    gerarNovaRnc();
                } else if (rnc.getEficaz().equals("Sim")) {
                    finalizarRnc();
                }
            } else {
                rnc = new Rnc();
                JsfUtil.addSuccessMessage("Concluído", "RNC salva com sucesso.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            JsfUtil.addErrorMessage("ERRO", "Erro ao salvar RNC.");
        }
    }

    public void clearFields() {
    }

    public void editar() {
        Rnc rnc = (Rnc) JsfUtil.getObjectFromRequestParameter("rnc");
        this.rnc = rnc;
        ((GerenciadorPaginas) JsfUtil.getObjectFromSession("gerenciadorPaginas")).cadastroRNC();
    }

    public void cadastrar() {
        this.rnc = new Rnc();
        ((GerenciadorPaginas) JsfUtil.getObjectFromSession("gerenciadorPaginas")).cadastroRNC();
    }

    public void remover() {
        try {
            Rnc rnc = (Rnc) JsfUtil.getObjectFromSession("rnc");
            rncDao.remover(rnc);
        } catch (Exception e) {
            e.printStackTrace();
        }
        removerRNCDaSessao();
    }

    public void adicionarRNCnaSessao() {
        Rnc rnc = (Rnc) JsfUtil.getObjectFromRequestParameter("rnc");
        HttpSession hs = JsfUtil.getSession(false);
        hs.setAttribute("rnc", rnc);
    }

    public void removerRNCDaSessao() {
        HttpSession hs = JsfUtil.getSession(false);
        hs.removeAttribute("rnc");
    }

    public List<Rnc> getTodasRNCs() {
        Usuario u = JsfUtil.getUsuarioLogado();
        if (u.getPapel().getNome().equals("usuario")) {
            return rncDao.executeNamedQueryComParametros("RNC.buscaResponsavel", new String[]{"responsavel"}, new Object[]{u});
        } else {
            return rncDao.buscarTodos();
        }
    }

    public Rnc getCabRNC() {
        return cabRNC;
    }

    public void setCabRNC(Rnc cabRNC) {
        this.cabRNC = cabRNC;
    }

    public Date getDataAtual() {
        return new Date();
    }

    public void imprimir(Rnc rnc) {
        String caminho = "/relatorios/rnc/rnc.jasper";

        Map parametros = new HashMap();

        parametros.put("rnc", rnc);

        parametros.put("logo", getLogotipoImage());

        System.out.println(getClass().getResource("../../../../../../../../relatorios/rnc/").toString());

        parametros.put("SUBREPORT_DIR", getClass().getResource("../../../../../../../../relatorios/rnc/").toString());

        FacesContext context = FacesContext.getCurrentInstance();

        HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();

        //pega o caminho do arquivo .jasper da aplicação
        InputStream reportStream = context.getExternalContext().getResourceAsStream(caminho);

        response.setHeader("Content-Disposition", "attachment; filename=rnc.pdf");
        response.setContentType("application/pdf");
        response.setHeader("Pragma", "no-cache");
        try {
            ServletOutputStream servletOutputStream = response.getOutputStream();

            //envia para o navegador o PDF gerado
            JasperRunManager.runReportToPdfStream(reportStream, servletOutputStream, parametros, new JREmptyDataSource());
            servletOutputStream.flush();
            servletOutputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            context.responseComplete();
        }
    }

    public boolean getHabilitaDisposicao() {
        if (!JsfUtil.getUsuarioLogado().getPapel().getNome().equals("gerente")) {
            if (rnc.getId() == 0) {
                return false;
            }
            if (!JsfUtil.getUsuarioLogado().equals(rnc.getResponsavel())) {
                return false;
            }
        }
        return true;
    }

    public boolean getHabilitaAcaoContencao() {
        if (!JsfUtil.getUsuarioLogado().getPapel().getNome().equals("gerente")) {
            if (rnc.getId() == 0) {
                return false;
            }
            if (!JsfUtil.getUsuarioLogado().equals(rnc.getResponsavel())) {
                return false;
            }
        }
        return true;
    }

    public boolean getHabilitaCausaProvavel() {
        if (!JsfUtil.getUsuarioLogado().getPapel().getNome().equals("gerente")) {
            if (rnc.getId() == 0) {
                return false;
            }
            if (!JsfUtil.getUsuarioLogado().equals(rnc.getResponsavel())) {
                return false;
            }
        }
        return true;
    }

    public boolean getHabilitaAcaoProposta() {
        if (!JsfUtil.getUsuarioLogado().getPapel().getNome().equals("gerente")) {
            if (rnc.getId() == 0) {
                return false;
            }
            if (!JsfUtil.getUsuarioLogado().equals(rnc.getResponsavel())) {
                return false;
            }
        }
        return true;
    }

    public boolean getHabilitaAbrangenciaAcao() {
        if (!JsfUtil.getUsuarioLogado().getPapel().getNome().equals("gerente")) {
            if (rnc.getId() == 0) {
                return false;
            }
            if (!JsfUtil.getUsuarioLogado().equals(rnc.getResponsavel())) {
                return false;
            }
        }
        return true;
    }

    public boolean getHabilitaAnaliseEficacia() {
        if (!JsfUtil.getUsuarioLogado().getPapel().getNome().equals("gerente")) {
            if (rnc.getId() == 0) {
                return false;
            }
            if (!JsfUtil.getUsuarioLogado().equals(rnc.getResponsavel())) {
                return false;
            }
            if (!rnc.getAcoesConcluidas()) {
                return false;
            }
        }
        return true;
    }
}
