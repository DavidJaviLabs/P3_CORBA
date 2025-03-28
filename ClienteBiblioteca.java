import Biblioteca.*;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;
import org.omg.CORBA.*;
import java.util.Scanner;

public class ClienteBiblioteca {
    public static void main(String args[]) {
        try {
            // Inicializar el ORB (Object Request Broker)
            ORB orb = ORB.init(args, null);

            // Obtener referencia al servicio de nombres
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

            // Buscar la referencia del objeto (servidor) en el servicio de nombres
            String name = "GestionBiblioteca";
            GestionBiblioteca gestionBiblioteca = GestionBibliotecaHelper.narrow(ncRef.resolve_str(name));

            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.println("\nOpciones:");
                System.out.println("1. Buscar libro");
                System.out.println("2. Alquilar libro");
                System.out.println("3. Devolver libro");
                System.out.println("4. Solicitar una recomendación");
                System.out.println("5. Ver el libro más popular");
                System.out.println("6. Obtener un libro aleatorio");
                System.out.println("7. Salir");
                System.out.print("Selecciona una opción: ");
                int opcion = scanner.nextInt();
                scanner.nextLine();

                switch (opcion) {
                    case 1:
                        System.out.print("Ingrese el título del libro: ");
                        String titulo = scanner.nextLine();
                        Libro libroEncontrado = gestionBiblioteca.buscarLibro(titulo);
                        if ("No encontrado".equals(libroEncontrado.titulo)) {
                            System.out.println("Libro no encontrado.");
                            break;
                        }
                        System.out.println("El libro `" + libroEncontrado.titulo + "´ ha sido encontrado.");
                        break;
                    case 2:
                        System.out.print("Ingrese su nombre para alquilar un libro: ");
                        String nombreClienteAlquilar = scanner.nextLine();
                        System.out.print("Ingrese el ISBN del libro que quiere alquilar: ");
                        String isbnLibroAlquilar = scanner.nextLine();
                        boolean alquilado = gestionBiblioteca.prestarLibro(isbnLibroAlquilar, nombreClienteAlquilar);
                        System.out.println(alquilado ? ("Libro con ISBN " + isbnLibroAlquilar +  " ha sido alquilado con éxito.") : ("No se pudo alquilar el libro con ISBN " + isbnLibroAlquilar + "."));
                        break;
                    case 3:
                        System.out.print("Ingrese el ISBN del libro que quiere devolver: ");
                        String isbnLibroDevolver = scanner.nextLine();
                        org.omg.CORBA.BooleanHolder devuelto = new org.omg.CORBA.BooleanHolder();
                        gestionBiblioteca.devolverLibro(isbnLibroDevolver, devuelto);
                        System.out.println(devuelto.value ? ("El libro con ISBN " + isbnLibroDevolver + " ha sido devuelto con éxito.") : ("No se pudo devolver el libro con ISBN " + isbnLibroDevolver + "."));
                        break;
                    case 4:
                        System.out.print("Ingrese su nombre para recomendarle un libro: ");
                        String nombreCliente = scanner.nextLine();
                        String recomendacion = gestionBiblioteca.recomendarLibro(nombreCliente);
                        System.out.println(recomendacion);
                        break;
                    case 5:
                        String popular = gestionBiblioteca.libroMasPopular();
                        System.out.println("El libro más popular es: " + popular);
                        break;
                    case 6:
                        Libro aleatorio = gestionBiblioteca.libroAleatorio();
                        System.out.println("Libro aleatorio: " + aleatorio.titulo + " de " + aleatorio.autor);
                        break;
                    case 7:
                        System.out.println("Apagando cliente...");
                        return;
                    default:
                        System.out.println("Opción inválida.");
                }
            }
        } catch (Exception e) {
            System.out.println("Error: " + e);
            e.printStackTrace(System.out);
        }
    }
}
