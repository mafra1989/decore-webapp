package com.webapp.manhattan.view;

import java.io.Serializable;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import com.webapp.model.Configuracao;
import com.webapp.model.Usuario;
import com.webapp.repository.Configuracoes;
import com.webapp.repository.Usuarios;
import com.webapp.util.jsf.FacesUtil;

@Named
@SessionScoped
public class GuestPreferences implements Serializable {
	
	private static final long serialVersionUID = 1L;

	@Inject
	private Configuracoes configuracoes;
	
	@Inject
	private Usuarios usuarios;
	
	@Inject
	private Configuracao configuracao;
	
	public void inicializar() {
		if (FacesUtil.isNotPostback()) {
			
			User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();		
			Usuario usuario = usuarios.porLogin(user.getUsername());
			
			configuracao = configuracoes.porUsuario(usuario);
			layoutMode = configuracao.getLayoutMode();
			lightMenu = configuracao.isLightMenu();	
		}
	}
		

    public enum LayoutMode {
        SLIM,
        STATIC,
        OVERLAY,
        HORIZONTAL,
        TOGGLE
    };
        
    private String theme = "teal-yellow";

    private LayoutMode layoutMode = LayoutMode.SLIM;

    private boolean lightMenu = true;
                            
	public String getTheme() {		
		return theme;
	}
    
	public void setTheme(String theme) {
		this.theme = theme;
    }
    
    public LayoutMode getLayoutMode() {		
		return layoutMode;
	}
    
	public void setLayoutMode(LayoutMode layoutMode) {
		this.layoutMode = layoutMode;
    }

    public void updateLayoutMode(String mode) {
        this.layoutMode = LayoutMode.valueOf(mode);
        configuracao.setLayoutMode(layoutMode);
        configuracao = configuracoes.save(configuracao);
    }

    public boolean isLightMenu() {
        return this.lightMenu;
    }

    public void setLightMenu(boolean value) {
        this.lightMenu = value;
        configuracao.setLightMenu(lightMenu);
        configuracao = configuracoes.save(configuracao);
    }
    
    public String getLayoutStyleClass() {
        String layoutStyleClass;
        switch(this.layoutMode) {
            case SLIM:
                layoutStyleClass = "layout-slim";
            break;

            case STATIC:
                layoutStyleClass = "layout-static";
            break;

            case OVERLAY:
                layoutStyleClass = "layout-overlay";
            break;

            case HORIZONTAL:
                layoutStyleClass = "layout-horizontal";
            break;

            case TOGGLE:
                layoutStyleClass = "layout-toggle";
            break;

            default:
                layoutStyleClass = "layout-slim";
            break;
        }

        if(this.lightMenu) {
            layoutStyleClass += " layout-light";
        }

        return layoutStyleClass;
    }
}
