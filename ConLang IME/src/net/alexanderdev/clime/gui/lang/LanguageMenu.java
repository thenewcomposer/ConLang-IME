/****************************************************************
 *   ____            _                        ___ __  __ _____  *
 *  / ___|___  _ __ | |    __ _ _ __   __ _  |_ _|  \/  | ____| *
 * | |   / _ \| '_ \| |   / _` | '_ \ / _` |  | || |\/| |  _|   *
 * | |__| (_) | | | | |__| (_| | | | | (_| |  | || |  | | |___  *
 *  \____\___/|_| |_|_____\__,_|_| |_|\__, | |___|_|  |_|_____| *
 *                                    |___/                     *
 *                                                              *
 * Copyright © 2015, Christian Bryce Alexander                  *
 ****************************************************************/
package net.alexanderdev.clime.gui.lang;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import net.alexanderdev.clime.gui.Editor;
import net.alexanderdev.clime.util.FileIO;
import net.alexanderdev.clime.util.FileSystem;
import net.alexanderdev.clime.util.Language;
import net.alexanderdev.clime.util.XMLIO;

/**
 * @author Christian Bryce Alexander
 * @since Sep 24, 2015, 2:29:45 AM
 */
public class LanguageMenu extends JMenu {
	private static final long serialVersionUID = -8740899833173163560L;

	private Map<String, Language> languages = new HashMap<>();

	private ButtonGroup langGroup;

	private JMenuItem createLang, editLang, deleteLang, csv, xml;

	private JMenu importLang;

	public LanguageMenu(Editor editor) {
		super("LANGUAGE");

		File[] files = FileSystem.getLangs().listFiles();

		for (File file : files)
			addLanguage(file.getAbsolutePath());

		String[] names = languages.keySet().toArray(new String[languages.size()]);

		langGroup = new ButtonGroup();

		JRadioButtonMenuItem none = new JRadioButtonMenuItem("NONE");
		none.addActionListener(e -> {
			if (editor.getCurrentTab() != null)
				editor.getCurrentTab().setLanguage(null);
		});
		langGroup.add(none);
		none.setSelected(true);
		add(none);

		for (String name : names) {
			JRadioButtonMenuItem b = new JRadioButtonMenuItem(name);
			b.addActionListener(e -> {
				if (editor.getCurrentTab() != null)
					editor.getCurrentTab().setLanguage(languages.get(name));
			});
			langGroup.add(b);
			add(b);
		}

		add(new JSeparator());

		createLang = new JMenuItem("CREATE");
		createLang.addActionListener(e -> LanguageEditor.showCreateDialog(editor.getFrame()));
		add(createLang);

		importLang = new JMenu("IMPORT");

		csv = new JMenuItem("CSV");
		csv.addActionListener(e -> {
			JFileChooser chooser = new JFileChooser(FileSystem.getDocs());

			int choice = chooser.showOpenDialog(editor.getFrame());

			if (choice == JFileChooser.APPROVE_OPTION)
				LanguageEditor.showCSVImportDialog(editor.getFrame(), FileIO.readFile(chooser.getSelectedFile()));
		});
		importLang.add(csv);

		xml = new JMenuItem("XML");
		xml.setEnabled(false);
		// xml.addActionListener(e ->
		// LanguageEditor.showCreateDialog(editor.getFrame()));
		importLang.add(xml);

		add(importLang);

		editLang = new JMenuItem("EDIT");
		editLang.setEnabled(false);
		editLang.addActionListener(e -> LanguageEditor.showEditDialog(editor.getFrame()));
		add(editLang);

		deleteLang = new JMenuItem("DELETE");
		deleteLang.setEnabled(false);
		deleteLang.addActionListener(e -> LanguageEditor.showDeleteDialog(editor.getFrame()));
		add(deleteLang);
	}

	private void addLanguage(String path) {
		try {
			Document doc = XMLIO.read(path, false);

			Element lang = doc.getDocumentElement();

			String langName = lang.getAttribute("name");
			String fontName = lang.getAttribute("font");
			int fontSize = Integer.parseInt(lang.getAttribute("size"));

			Element map = (Element) lang.getElementsByTagName("map").item(0);

			NodeList sets = map.getElementsByTagName("set");

			int max = 1;

			Map<String, String> langMap = new HashMap<>();
			Map<String, String> escapes = new HashMap<>();

			for (int i = 0; i < sets.getLength(); i++) {
				Element set = (Element) sets.item(i);

				String key = set.getAttribute("key");
				String val = set.getAttribute("val");
				escapes.put("\\" + key, key);
				langMap.put(key, val);

				if (key.length() > max)
					max = key.length();
			}

			languages.put(langName, new Language(langName, fontName, fontSize, langMap, escapes, max));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}