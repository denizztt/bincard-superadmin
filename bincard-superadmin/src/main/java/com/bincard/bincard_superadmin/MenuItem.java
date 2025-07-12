package com.bincard.bincard_superadmin;

import java.util.ArrayList;
import java.util.List;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

/**
 * Menü öğesi için model sınıfı.
 * Ana menü öğeleri ve alt menü öğelerini temsil eder.
 */
public class MenuItem {
    private String title;
    private String color;
    private List<MenuItem> subItems;
    private String targetPage; // Alt menü öğesi için hangi sayfayı açacağını belirler
    private FontAwesomeSolid icon; // Menü öğesi için ikon

    public MenuItem(String title, String color) {
        this.title = title;
        this.color = color;
        this.subItems = new ArrayList<>();
        this.targetPage = null;
        this.icon = FontAwesomeSolid.CIRCLE; // Varsayılan ikon
    }
    
    public MenuItem(String title, String color, String targetPage) {
        this(title, color);
        this.targetPage = targetPage;
    }
    
    public MenuItem(String title, String color, FontAwesomeSolid icon) {
        this(title, color);
        this.icon = icon;
    }
    
    public MenuItem(String title, String color, FontAwesomeSolid icon, String targetPage) {
        this(title, color);
        this.icon = icon;
        this.targetPage = targetPage;
    }
    
    public void addSubItem(MenuItem item) {
        subItems.add(item);
    }
    
    public String getTitle() {
        return title;
    }
    
    public String getColor() {
        return color;
    }
    
    public List<MenuItem> getSubItems() {
        return subItems;
    }
    
    public boolean hasSubItems() {
        return !subItems.isEmpty();
    }
    
    public String getTargetPage() {
        return targetPage;
    }
    
    public FontAwesomeSolid getIcon() {
        return icon;
    }
    
    public void setIcon(FontAwesomeSolid icon) {
        this.icon = icon;
    }
    
    public boolean isSubItem() {
        return targetPage != null;
    }
}
