package com.rever.files;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.rever.files.xml.Column;
import com.rever.files.xml.Column.ColumnType;
import com.rever.files.xml.Entity;

public class XMLExtractor {

	private static final String ENTITY_TAG = "entity";
	private static final String CLASS_TYPE = "class";
	private static final String ID_TAG = "id";
	private static final String BASIC_TAG = "basic";
	private static final String TABLE_TAG = "table";
	private static final String ATTRIBUTE_NAME = "name";
	private static final String PROPERTY_TAG = "property";
	private static final Object CONNECTION_ATTRIBUTE = "openjpa.ConnectionURL";
	private static final String VALUE_ATTRIBUTE = "value";
	private String xmlPath;

	public XMLExtractor(String xmlPath) {
		this.xmlPath = xmlPath;
	}

	/**
	 * @return la lista de entidades con sus columnas
	 */
	public List<Entity> extractEntities() {
		List<Entity> entities = new ArrayList<Entity>();
		try {
			File fXmlFile = new File(getXmlPath());
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();
			/*
			 * Extrae primero las entidades
			 */
			NodeList xmlEntities = doc.getElementsByTagName(ENTITY_TAG);
			/*
			 * Recorrelas
			 */
			for (int j = 0; j < xmlEntities.getLength(); j++) {

				Node xmlEntity = xmlEntities.item(j);

				if (xmlEntity.getNodeType() == Node.ELEMENT_NODE) {

					Element xmlEntityElement = (Element) xmlEntity;

					Entity entity = new Entity();
					/*
					 * Extrae su nombre
					 */
					entity.setName(xmlEntityElement.getAttribute(CLASS_TYPE));
					/*
					 * Extrae sus atributos
					 */

					List<Column> columns = new ArrayList<Column>();

					NodeList tables = xmlEntityElement.getElementsByTagName(TABLE_TAG);
					Element table = (Element) tables.item(0);

					entity.setTableName(table.getAttribute(ATTRIBUTE_NAME));

					NodeList ids = xmlEntityElement.getElementsByTagName(ID_TAG);
					for (int i = 0; i < ids.getLength(); i++) {
						Element id = (Element) ids.item(i);
						columns.add(getDefinition(id).setColumnType(ColumnType.ID));
					}

					NodeList basics = xmlEntityElement.getElementsByTagName(BASIC_TAG);
					for (int i = 0; i < basics.getLength(); i++) {
						Element basic = (Element) basics.item(i);
						columns.add(getDefinition(basic).setColumnType(ColumnType.BASIC));
					}
					entity.setColumns(columns);
					entities.add(entity);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return entities;
	}

	/**
	 * @param element the xml element
	 * @return la columna formada
	 */
	private Column getDefinition(Element element) {
		NodeList definitions = element.getElementsByTagName("column");
		Element definition = (Element) definitions.item(0);
		Column column = new Column();
		column.setColumnDefinition(definition.getAttribute("column-definition"));
		column.setName(definition.getAttribute("name"));
		try {
			int length = Integer.parseInt(definition.getAttribute("length"));
			column.setLength(length);
		} catch (NumberFormatException ex) {
		}
		String nullable = definition.getAttribute("nullable");
		if (nullable != null && nullable.equals("")) {
			column.setNullable(nullable.equals("true"));
		}

		return column;
	}

	public String getDatabaseName() {
		try {
			File fXmlFile = new File(getXmlPath());
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();

			NodeList xmlEntities = doc.getElementsByTagName(PROPERTY_TAG);
			for (int i = 0; i < xmlEntities.getLength(); i++) {
				Element property = (Element) xmlEntities.item(i);
				String name = property.getAttribute(ATTRIBUTE_NAME);
				if (name.equals(CONNECTION_ATTRIBUTE)) {
					String separator = "\\";
					String connection = property.getAttribute(VALUE_ATTRIBUTE);
					String databaseName = connection.substring(connection.indexOf("//") + 2, connection.length());
					return databaseName.split("/")[databaseName.split("/").length - 1];
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private String getXmlPath() {
		return xmlPath;
	}

	private void setXmlPath(String xmlPath) {
		this.xmlPath = xmlPath;
	}
}