import Biblioteca.*;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;
import org.omg.CORBA.*;
import org.omg.PortableServer.*;
import org.omg.PortableServer.POA;
import java.util.*;

class GestionBibliotecaImpl extends GestionBibliotecaPOA {
    private ORB orb;
    private Map<String, Libro> libros = new HashMap<>();
    private Map<String, Integer> popularidad = new HashMap<>();
    private Map<String, List<String>> historialUsuarios = new HashMap<>();

    public GestionBibliotecaImpl(ORB orb) {
        this.orb = orb;
        // Inicializar algunos libros en el sistema
        libros.put("1234", new Libro("El principito", "Antoine de Saint-Exupéry", "1234", true));
        libros.put("5678", new Libro("1984", "George Orwell", "5678", true));
        libros.put("9101", new Libro("Cien años de soledad", "Gabriel García Márquez", "9101", true));
        libros.put("6544", new Libro("Don Quijote de la Mancha", "Miguel de Cervantes", "6544", true));
        libros.put("7890", new Libro("Orgullo y prejuicio", "Jane Austen", "7890", true));
        libros.put("2389", new Libro("Matar a un ruiseñor", "Harper Lee", "2389", true));
        libros.put("9043", new Libro("Crimen y castigo", "Fiódor Dostoievski", "9043", true));
        libros.put("9469", new Libro("Ulises", "James Joyce", "9469", true));
        libros.put("9340", new Libro("En busca del tiempo perdido", "Marcel Proust", "9340", true));
        libros.put("8036", new Libro("Los miserables", "Victor Hugo", "8036", true));
        libros.put("5543", new Libro("El gran Gatsby", "F. Scott Fitzgerald", "5543", true));
        libros.put("3456", new Libro("La Odisea", "Homero", "3456", true));
        libros.put("5683", new Libro("Fahrenheit 451", "Ray Bradbury", "5683", true));
        // Agregar más libros según sea necesario
    }

    public void setORB(ORB orb_val) { 
        orb = orb_val; 
    }

    // Implementar los métodos de la interfaz GestionBiblioteca
    @Override
    public Libro buscarLibro(String titulo) {
        return libros.values().stream()
                .filter(libro -> libro.titulo.equals(titulo) && libro.estaDisponible)
                .findFirst()
                .orElse(new Libro("No encontrado", "", "", false));
    }

    @Override
    public boolean prestarLibro(String ISBN, String usuario) {
        if (libros.containsKey(ISBN) && libros.get(ISBN).estaDisponible) {
            libros.get(ISBN).estaDisponible = false;
            popularidad.put(ISBN, popularidad.getOrDefault(ISBN, 0) + 1);
            
            // Actualizar el historial del usuario
            historialUsuarios.putIfAbsent(usuario, new ArrayList<>());
            historialUsuarios.get(usuario).add(ISBN);
            return true;
        }
        return false;
    }

    // DEVOLVER LIBRO HACERLO VOID Y DE OUT PONER EL BOOLEAN
    @Override
    public void devolverLibro(String ISBN, org.omg.CORBA.BooleanHolder devuelto) {
        if (libros.containsKey(ISBN) && !libros.get(ISBN).estaDisponible) {
            libros.get(ISBN).estaDisponible = true;
            devuelto.value = true;
        }
        else {
            devuelto.value = false;
        }
    }
    
    // Recomendar un libro según historial de usuario
    public String recomendarLibro(String usuario)
    {
        List<String> leidos = historialUsuarios.getOrDefault(usuario, new ArrayList<>());
        for (Libro libro: libros.values())
        {
            if (!leidos.contains(libro.ISBN) && libro.estaDisponible)
            {
                return "Te recomendamos leer: " + libro.titulo + " de " + libro.autor;
            }
        }
        return usuario + ", no hay recomendaciones disponibles para ti.";
    }

    // Obtener el libro más popular
    public String libroMasPopular()
    {
        return popularidad.entrySet().stream()
                .max(Comparator.comparingInt(Map.Entry::getValue))
                .map(entry -> libros.get(entry.getKey()).titulo + " con " + entry.getValue() + " préstamos.")
                .orElse("Aún no hay libros populares.");
    }
    
    // Obtener un libro aleatorio
    public Libro libroAleatorio()
    {
        List<Libro> disponibles = new ArrayList<>(libros.values());
        if (disponibles.isEmpty())
        {
            return new Libro("No disponible", "", "", false);
        }
        return disponibles.get(new Random().nextInt(disponibles.size()));
    }
}

public class ServidorBiblioteca {
    public static void main(String args[]) {
        try {
            // Crear e inicializar el ORB
            ORB orb = ORB.init(args, null);

            // Obtener referencia a rootpoa y activar el POAManager
            POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            rootpoa.the_POAManager().activate();

            // Crear el servicio e inscribirlo en el ORB
            GestionBibliotecaImpl gestionBiblioteca = new GestionBibliotecaImpl(orb);
            org.omg.CORBA.Object ref = rootpoa.servant_to_reference(gestionBiblioteca);
            GestionBiblioteca href = GestionBibliotecaHelper.narrow(ref);

            // Obtener referencia al servicio de nombres
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

            // Vincular la referencia del objeto en el servicio de nombres
            String name = "GestionBiblioteca";
            NameComponent path[] = ncRef.to_name(name);
            ncRef.rebind(path, href);

            System.out.println("El servidor de la biblioteca está listo y esperando ...");

            // Esperar llamadas de los clientes
            orb.run();
        } catch (Exception e) {
            System.err.println("Error: " + e);
            e.printStackTrace(System.out);
        }
    }
}