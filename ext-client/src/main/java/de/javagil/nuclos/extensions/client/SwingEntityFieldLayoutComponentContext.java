package de.javagil.nuclos.extensions.client;

import java.awt.event.MouseListener;
import java.io.File;

import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.validation.constraints.NotNull;

import de.javagil.nuclos.extensions.client.impl.NuclosEntityFieldLayoutComponentImpl;

/**
 * Schnittstelle von einer Swing-Komponente zu einer {@link NuclosEntityFieldLayoutComponent}.
 * Die Swing-Komponente bleibt dadurch völlig frei von Nuclos Typen und ist damit eigenständig testbar.
 * 
 * @author (C)2015 <a href="mailto:michael@hoennig.de">Michael Hoennig</a> for HGS GmbH
 * @license http://de.wikipedia.org/wiki/GNU_Affero_General_Public_License
 */
public interface SwingEntityFieldLayoutComponentContext<FT> {

	/**
	 * Setzt den Wert des Entitäten-Feldes.
	 * 
	 * @param value neuer Wert
	 * @return indiziert, ob der neue Wert in das Feld gesetzt werden konnte  
	 */
	boolean setFieldValue(String value);

	/**
	 * Fragt eine Umgebungs-Eigenschaft ab.
	 * 
	 * @param propName Name der Eigenschaft
	 * @return der Eigenschaftswert
	 */
	Object getProperty(@NotNull String propName);

	/**
	 * Liefert eine voll formatierte Nachricht, die Parameter sind also in die Vorlage eingefügt.
	 * 
	 * @param rid  ID der Resource
	 * @param oName ???
	 * @param params Parameter, passend zur Vorlage unter der genannten Resourcen-ID
	 * @return die formatierte Nachricht
	 * @see java.text.MessageFormat
	 */
	public String getMessage(@NotNull String rid, @NotNull String otext, Object ... params);

	/**
	 * Erzeugt einen MouseListener für das angegebene Popup-Menü.
	 * 
	 * @param newJPopupMenu das Popup-Menü
	 * @return der MouseListener
	 */
	MouseListener createMouseListener(@NotNull JPopupMenu newJPopupMenu);

	/**
	 * Führt (ggf. asynchron) das übergebene Kommando aus.
	 * 
	 * @param component Kontext Komponente
	 * @param command das auszuführende Kommando
	 */
	void runCommand(@NotNull JComponent component, @NotNull Runnable command);

	/**
	 * Öffnet die übergebene Datei über das Betriebssystem mit einem passenden Programm.
	 * 
	 * @param file zu öffnende Datei
	 */
	void openFile(@NotNull File file);

	/**
	 * @return the name of the field 
	 */
	String getFieldName();

	/**
	 * @return the current value of the field
	 */
	FT getFieldValue();
	
	/**
	 * @return true if context is layout editor, false if context is data viewing/editing 
	 */
	boolean isLayoutMode();
}
