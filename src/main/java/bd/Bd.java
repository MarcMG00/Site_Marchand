package bd;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;


/**
 * Classe en charge de la base de données.
 */
public class Bd
{
	/*---------*/
	/* Données */
	/*---------*/

	/*----- Connexion -----*/
	private static Connection cx = null;

	/*----- Données de connexion -----*/
	private static final String URL			= "srv1049.hstgr.io";
	private static final String LOGIN		= "u523250608_projetdai";
	private static final String PASSWORD	= "Projetdai1$";


	/*----------*/
	/* Méthodes */
	/*----------*/

	/**
	 * Crée la connexion avec la base de données.
	 */
	private static void connexion() throws ClassNotFoundException, SQLException
	{
		/*----- Chargement du pilote pour la BD -----*/
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		}
		catch (ClassNotFoundException ex)
		{
			throw new ClassNotFoundException("Exception Bd.connexion() - Pilote MySql introuvable - " + ex.getMessage());
		}

		/*----- Ouverture de la connexion -----*/
		try {
			Bd.cx = DriverManager.getConnection(URL,LOGIN,PASSWORD);
		}
		catch (SQLException ex)
		{
			throw new SQLException("Exception Bd.connexion() - Problème de connexion à la base de données - " + ex.getMessage());
		}
	}


	/**
	 * Retourne la liste de citations de 'nom_auteur'.
	 */
	public static ArrayList<String> lireCitations (String nom_auteur) throws ClassNotFoundException, SQLException
	{
		/*----- Création de la connexion à la base de données -----*/
		if (Bd.cx == null)
			Bd.connexion();

		/*----- Interrogation de la base -----*/
		ArrayList<String> liste = new ArrayList<>();

		/*----- Requête SQL -----*/
		String sql = "SELECT LibCitation FROM Auteur, Citation WHERE Auteur.IdAuteur=Citation.AutCitation AND Auteur.NomAuteur=?";

		/*----- Ouverture de l'espace de requête -----*/
		try (PreparedStatement st = Bd.cx.prepareStatement(sql))
		{
			/*----- Exécution de la requête -----*/
			st.setString(1, nom_auteur);
			try (ResultSet rs = st.executeQuery())
			{
				/*----- Lecture du contenu du ResultSet -----*/
				while (rs.next())
					liste.add(rs.getString(1));
			}
		}
		catch (SQLException ex)
		{
			throw new SQLException("Exception Bd.lireCitations() : Problème SQL - " + ex.getMessage());
		}

		return liste;
	}

	/**
	 * Retourne la liste de mots contenant les mêmes lettres saisies.
	 */
	public static ArrayList<String> rechercheGoogle (String chaine) throws ClassNotFoundException, SQLException
	{
		/*----- Création de la connexion à la base de données -----*/
		if (Bd.cx == null)
			Bd.connexion();

		/*----- Interrogation de la base -----*/
		ArrayList<String> listeMotsContenantChaine = new ArrayList<>();

		if(chaine.isEmpty() || chaine == null) {
			listeMotsContenantChaine.add("Nothing to show");

		} else {
			/*----- Requête SQL -----*/
			String sql = "SELECT Texte FROM Mot WHERE Texte LIKE ?";

			/*----- Ouverture de l'espace de requête -----*/
			try (PreparedStatement st = Bd.cx.prepareStatement(sql))
			{
				/*----- Exécution de la requête -----*/
				st.setString(1, chaine+"%");
				try (ResultSet rs = st.executeQuery())
				{
					/*----- Lecture du contenu du ResultSet -----*/
					while (rs.next()) {
						listeMotsContenantChaine.add(rs.getString(1));
					}
				}
			}
			catch (SQLException ex)
			{
				throw new SQLException("Exception Bd.rechercheGoogle() : Problème SQL - " + ex.getMessage());
			}
		}

		return listeMotsContenantChaine;
	}


	// Sélection de tous les messages de la BD
	public static ArrayList<String> getAllMessages() throws Exception {
		if(cx == null) {
			connexion();
		}
		ArrayList<String> allMsg = new ArrayList<String>();
		// Requête de sélection
		String query = "SELECT Texte FROM Mot";

		try (PreparedStatement st = cx.prepareStatement(query)){
			// Insertion des paramètres
			ResultSet rs = st.executeQuery();
			while(rs.next()) {
				allMsg.add(rs.getString("Texte"));
			}

		} catch (SQLException e) {
			throw new Exception("Bd.getAllMessages() - " + e.getMessage());
		}
		return allMsg;
	}

	// Sélection de tous les messages de la BD
	public static boolean checkExistingWord(String chaine) throws Exception {
		if(cx == null) {
			connexion();
		}
		boolean isWordExisting = false;

		/*----- Requête SQL -----*/
		String sql = "SELECT Texte FROM Mot WHERE Texte = ?";

		/*----- Ouverture de l'espace de requête -----*/
		try (PreparedStatement st = Bd.cx.prepareStatement(sql))
		{
			/*----- Exécution de la requête -----*/
			st.setString(1, chaine);
			try (ResultSet rs = st.executeQuery())
			{
				/*----- Lecture du contenu du ResultSet -----*/
				if(rs.next()) {
					isWordExisting = true;
				}
			}
		} catch (SQLException e) {
			throw new Exception("Bd.getAllMessages() - " + e.getMessage());
		}
		return isWordExisting;
	}

	// Insertion d'un message à la BD
	public static int addMessage(String word) throws Exception {
		if(cx == null) {
			connexion();
		}
		int nb = 0;
		// Requête d'insertion
		String query = "INSERT INTO Mot (Texte) VALUES (?)";

		try (PreparedStatement st = cx.prepareStatement(query)){
			// Insertion des paramètres
			st.setString(1, word);

			nb = st.executeUpdate();

		} catch (SQLException e) {
			throw new Exception("Bd.addMessage() - " + e.getMessage());
		}
		return nb;
	}

	// Suppression du mot sélectionné dans la BD
	public static int deleteWord(String wordToDelete) throws Exception {
		if(cx == null) {
			connexion();
		}
		int nb = 0;

		// Requête de suppression
		String query = "DELETE FROM Mot WHERE Texte = ?";
		try (PreparedStatement st = cx.prepareStatement(query)){
			// Exécution de la suppression
			st.setString(1, wordToDelete);
			st.executeUpdate();
			nb += 1;

		} catch (SQLException e) {
			throw new Exception("Bd.deleteWord() - " + e.getMessage());
		}
		return nb;
	}

	// Suppression du mot sélectionné dans la BD
	public static int countWords() throws Exception {
		if(cx == null) {
			connexion();
		}
		int nbWords = 0;

		// Requête COUNT
		String query = "SELECT COUNT(*) FROM Mot";
		try (PreparedStatement st = cx.prepareStatement(query)){
			// Exécution de la requête
			ResultSet rs = st.executeQuery();
			rs.next();
			nbWords = rs.getInt(1);

		} catch (SQLException e) {
			throw new Exception("Bd.deleteWord() - " + e.getMessage());
		}
		return nbWords;
	}

	/*----------------------------*/
	/* Programme principal (test) */
	/*----------------------------*/

	public static void main (String[] s)
	{
//		try {
//			ArrayList<String> l = Bd.lireCitations("Coluche");
//			for (String msg : l) System.out.println(msg);
//		}
//
//		catch (ClassNotFoundException | SQLException ex)
//		{
//			System.out.println(ex.getMessage());
//		}

		try {
			System.out.println(countWords());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

} /*----- Fin de la classe Bd -----*/
