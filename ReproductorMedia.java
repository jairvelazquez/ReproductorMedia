import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.*;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import com.sun.jna.NativeLibrary;

import java.awt.*;
import uk.co.caprica.vlcj.component.*;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;
import uk.co.caprica.vlcj.player.MediaPlayer; 
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;

/* Examen Segundo Parcial Interaccion Humano-Maquina
 * Elias Jair Velazquez Espinoza 
 * Ramon Eduardo Ascencio Bribiesca 
 */
//https://www.html5rocks.com/es/tutorials/video/basics/Chrome_ImF.mp4
public class ReproductorMedia extends JFrame implements ActionListener {
	
	/* Declaracion de elementos graficos a utilizar*/ 
	JFileChooser jcSel;
	JLabel tiempoTotal,tiempoInicial;
	JPanel panBotones,panBarra,panCheckbox;
	JCheckBox volumenTiempo;
	JButton bBotonAbrir,bBotonPlayPause,bBotonStop;

	JSlider BarraVolumen,BarraTiempo;
	JPanel barraTiempoTotal;
	JLabel tiempoLlevado, tiempoRestante;
	
	
	/* Declaracion de variables de control de tiempo y volumen*/ 
	long lTiempoLlevado,lTiempoRestante;
	long tiempoDeReproduccion;
	long calculoTiempos;
	int estadoVolumen;
    int stopped;
    float currentVolume;
    float currentMedia;
    
	/* Booleanos con la explicacion de su funcionamiento*/
	boolean pause = true;
	boolean archivoCargado=false;
    boolean band;
	int playpause; // a veces se quedan las costumbres de C 
	int antesBarraTiempo=0;
	
	/* Declaracion de elementos pertenecientes al reproducto otorgadas por vlcj*/ 
	EmbeddedMediaPlayerComponent reproductor;
	
	//declaraciones adicionales acompa�adas de su funcionamiento*/ 
	final String sPATH_VLC;
    CronometroA crono;
    Runnable hilo;

    /* Declaracion de variables de FILE usadas para la recuperacion de un archivo local*/
    File fArchivo;    
	String path;
	FileNameExtensionFilter filtroExtensiones; /* Filtro para que solo se puedan escoger archivos de tipo video*/
	
	private void iniciaComponentes() {
		panBotones = new JPanel(new GridLayout(1,3,5,5));
		barraTiempoTotal = new JPanel(new BorderLayout());
		panBarra = new JPanel(new BorderLayout());
		panCheckbox = new JPanel(new FlowLayout());
		
		volumenTiempo = new JCheckBox();
		volumenTiempo.setText("Controlando Volumen");
		volumenTiempo.setSelected(true);
		
		bBotonAbrir = new JButton(new ImageIcon(getClass().getResource("Boton File.jpg")));
		bBotonStop = new JButton(new ImageIcon(getClass().getResource("Boton Stop.jpg")));
		bBotonPlayPause = new JButton(new ImageIcon(getClass().getResource("Boton Play.jpg")));
		
		
		tiempoTotal = new JLabel("00:00");
		tiempoInicial = new JLabel("00:00");
		tiempoLlevado= new JLabel("00:00");
		tiempoRestante = new JLabel("00:00");
		
		bBotonPlayPause.setEnabled(false);
		bBotonStop.setEnabled(false);
		
		BarraVolumen = new JSlider(SwingConstants.HORIZONTAL,0,100,10);
		BarraVolumen.setMajorTickSpacing(10);
		
		BarraVolumen.setPaintTicks(true);
		
		BarraVolumen.setPaintLabels(true);
		
		panBotones.add(bBotonAbrir);
		panBotones.add(bBotonStop);
		panBotones.add(bBotonPlayPause);
		
		bBotonAbrir.addActionListener(this);
		bBotonStop.addActionListener(this);
		bBotonPlayPause.addActionListener(this);
		volumenTiempo.addActionListener(this);
		
		panCheckbox.add(volumenTiempo,FlowLayout.LEFT);
		
		panBarra.add(tiempoLlevado);
		
		panBarra.add(tiempoRestante);
		panBarra.add(panCheckbox,BorderLayout.SOUTH);
		
		//panTodo.add(panBarra);
		//panTodo.add(panBotones);
		
		panBarra.add(tiempoLlevado,BorderLayout.WEST);
		
		panBarra.add(BarraVolumen,BorderLayout.CENTER);
		
		panBarra.add(tiempoRestante,BorderLayout.EAST);
		
		
		
		barraTiempoTotal.add(tiempoInicial,BorderLayout.WEST);
		barraTiempoTotal.add(tiempoTotal,BorderLayout.EAST);
		panBarra.add(barraTiempoTotal,BorderLayout.NORTH);
	}
	private void iniciaHerramientas() {
		crono = new CronometroA();
		jcSel = new JFileChooser();
		NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(),sPATH_VLC);
		
		reproductor = new EmbeddedMediaPlayerComponent();
		filtroExtensiones = new FileNameExtensionFilter("Tipos de Media","mp3","mp4","mkv","avi","mpeg");
		
		reproductor = new EmbeddedMediaPlayerComponent();
		path = "";
		
		currentVolume = 95;
        estadoVolumen=95;
        currentMedia = 0;
	}
	public ReproductorMedia () {
            
                sPATH_VLC = "C:/Program Files/VideoLAN/VLC";
		iniciaComponentes();
		iniciaHerramientas();
		

		this.setTitle("Reproductor de Media");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(1100,600);
		this.setLocation(0,0);
		
		/* Se agrega dos metodos para los eventos del JSlider, uno de tipo ChangeListener y el otro
		 * de tipo MediaPlayerEvent Listener 
		 * */
		
        BarraVolumen.addChangeListener(new ChangeListener() {
        
            @Override
            public void stateChanged(ChangeEvent e) {
                Object source = e.getSource();  
                if(volumenTiempo.isSelected()==true) {
                	if(antesBarraTiempo!=0) {
                		BarraVolumen.setValue(estadoVolumen);
                		antesBarraTiempo=0;
                	}
                    reproductor.getMediaPlayer().setVolume( ((JSlider) source).getValue() );
                    currentVolume = ((JSlider) source).getValue();
                }
            }
        });
            
        reproductor.getMediaPlayer().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
            
            public void positionChanged(MediaPlayer mp, float pos) {
            	
                if(volumenTiempo.isSelected()==false) {
                	antesBarraTiempo = 1;
                	estadoVolumen = BarraVolumen.getValue();
                    if(band){
                        int value = Math.min(100, Math.round(pos * 100.0f));  
                      
                        lTiempoLlevado = (tiempoDeReproduccion*value)/100; //se calcula el nuevo tiempo de reproduccion
                    	lTiempoRestante = tiempoDeReproduccion-lTiempoLlevado;//se caulcula el nuevo tiempo restante
                        BarraVolumen.setValue(value);//se le ajusta el valor a la barra
                        currentMedia = value; // se acualiza el valor actual de la media
                        band = false; //bandera de soltado de clic vuelve a su estado original
                    }
                }
            }
            @Override
            public void finished(MediaPlayer mediaPlayer) {
            }
        });
            /* Este evento escucha al mouse para unicamente cambiar la posicion del video
             * Cuando se ha soltado el mouse, se podria utilizar una clase aparte con un adaptador 
             * para ahorrar lineasde codigo 
             */
        BarraVolumen.addMouseListener(new MouseListener(){

            @Override
            public void mouseClicked(MouseEvent e) {}
            @Override
            public void mousePressed(MouseEvent e) {
                band= false;
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                band = true;
            }
            @Override
            public void mouseEntered(MouseEvent e) {}
            @Override
            public void mouseExited(MouseEvent e) {}
        });
            
       /* Evento que escucha y modifica a la barra de volumen*/ 
        BarraVolumen.addChangeListener(new ChangeListener(){

            @Override
            public synchronized void stateChanged(ChangeEvent e) {
                if(volumenTiempo.isSelected()==false) {
                    if( !band ){
                        Object source = e.getSource();                                
                        float np = ((JSlider) source).getValue() / 100f;                    
                        reproductor.getMediaPlayer().setPosition(np);//Se cambia la posicion del reproductor obteniendo el valor de la barra
                    }
                }
            }
        });

		this.add(panBarra,BorderLayout.NORTH);
		this.add(reproductor,FlowLayout.CENTER);
		this.add(panBotones,BorderLayout.SOUTH);
		this.setVisible(true);
		this.setResizable(true);
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new ReproductorMedia();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		// TODO Auto-generated method stub
		if (e.getSource().equals(bBotonAbrir)) {
			
			archivoCargado = false;//Como se acaba de pulsar el boton se ajusta el estado de archivo cargado a falso
			
			bBotonPlayPause.setEnabled(false);
			bBotonStop.setEnabled(false);
			path = "";
			
			int decision; 
			String[] opcion = {"Archivo Local","Archivo Remoto"};
			
			decision = JOptionPane.showOptionDialog(this, "Tipo de archivo a seleccionar", "Abriendo archivo", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, opcion, opcion[0]);
			
			if (decision == 0) { //Si se tomo la decision de un archivo local
				jcSel.setFileFilter(filtroExtensiones); //se ajusta el filtro de las extensiones
				jcSel.showOpenDialog(null);
				fArchivo = jcSel.getSelectedFile();
				if (fArchivo !=null) {
					path = fArchivo.getAbsolutePath();//se obtiene el path seleccionado por el usuario
					cargarMedia(path);//cargamos la media por medio del path
				}
			}
			
			else if (decision == 1) {//si se tomo la decision del archivo remoto
				//path = "https://www.quirksmode.org/html5/videos/big_buck_bunny.mp4";
				
				path = JOptionPane.showInputDialog("Inserte el URL de donde se localiza el video");
				cargarMedia(path);
			}
			if (archivoCargado) {//si el archivo se cargo correctamente se ajustan los componentes para empezar la reproduccion
				BarraVolumen.setValue(reproductor.getMediaPlayer().getVolume());
                                
				bBotonPlayPause.setEnabled(true);
				bBotonStop.setEnabled(true);
			}
			else if (!archivoCargado) {//si el archivo no llego a cargarse se anuncia el fallo
				JOptionPane.showMessageDialog(this, "Fallo al cargar del archivo, "
						+ "intente con una nueva ruta o formato");
			}
			
		}
		else if (e.getSource().equals(bBotonPlayPause)&&archivoCargado) {//si se usa el boton de play/pausa y si hay un archivo cargado
			
			if (pause) {//si esta en estado de pausa se empieza a reproducir 
				reproductor.getMediaPlayer().play();
				new Thread(hilo).start();//comienza el hilo para obtener el tiempo total del video
				crono.start();
				intercambiaEstado();//funcion intercambia estado se explicara en la parte de abajo
			}
			else if(!pause) {//si esta en estado de play se pausa 
				reproductor.getMediaPlayer().pause();  
				crono.stop();
				intercambiaEstado();
			}
			
			
		}
		else if (e.getSource().equals(bBotonStop)) {//si se selecciono el boton de stop
			reproductor.getMediaPlayer().stop();
			crono.stop();
			intercambiaEstado();
			lTiempoLlevado=0;lTiempoRestante=0;//se ajustan los tiempos a 0 ya que el video fue pausado
			
		}

	}
	
	public void cargarMedia(String path) {//funcion que carga la media se recibe el path por parametro
		try {
			reproductor.getMediaPlayer().prepareMedia(path);
			archivoCargado=true;
			hilo = new Hilo(reproductor.getMediaPlayer()); //se inicializa el hilo con el archivo ya cargado
		}catch(Exception e) {
			JOptionPane.showMessageDialog(null, "Fallo la carga del archivo");
		}
		
	}
	

	public void intercambiaEstado() {//funcion cambiar estado, unicamente se encarga de ajustar el estado de "pause" e intercambiar los iconos
		if (pause) {
			bBotonPlayPause.setIcon(new ImageIcon(getClass().getResource("Boton Pause.jpg")));
			pause = false;
		}
		else if(!pause) {
			
			bBotonPlayPause.setIcon(new ImageIcon(getClass().getResource("Boton Play.jpg")));
			
			pause = true;
		}
	}

	public class Hilo implements Runnable{//Hilo

	    EmbeddedMediaPlayer player = null;

	    public Hilo(EmbeddedMediaPlayer player){//constructor que recibe el player por parametro
	        this.player = player;
	    }

	    @Override
	    public void run() {
	        try {
                    /*La funcion se encarga de esperar a que el archivo este en reproduccion y buffereado 
                    para poder obtener el tiempo total, ya que de pedirlo en la funcion de play el archivo aun no estara 
                    reproduciendose y arrojara un -1, deberás ajustar el tiempo si es necesario */
	            Thread.sleep(4000);  
	            tiempoDeReproduccion = (long)player.getMediaMeta().getLength() / 1000;//se cambia a segundos
	            tiempoTotal.setText(calculaTiempo(tiempoDeReproduccion));//el tiempo total es llenado de una vez, la funcion "calcula tiempo" se explicara mas abajo
	            lTiempoRestante = tiempoDeReproduccion;//Se ajusta el valor del tiempo total 
	            System.out.println(tiempoDeReproduccion); //Control en consola del tiempo total, se notara que aparece milisegundos
	            

	        } catch (InterruptedException e) {
	            e.printStackTrace();
	        }
	    }
	}
	public String calculaTiempo(long tiempo) {//Funcion calcula tiempo, recibe los segundos por parametro y regresa una cadena con el formato de hora
		int horas,minutos;
		int rMinuto,rSegundo;
		String cadena;
		
		horas = (int) tiempo/3600;
		rMinuto = (int) tiempo%3600;
		
		minutos = (int) rMinuto/60;
		rSegundo = (int) rMinuto%60;
	
		
		cadena = horas + ":" + minutos + ":" + rSegundo;
		return cadena;
	}
	class CronometroA{//Clase cromoetro que realiza todo el trabajo de las etiquetas de tiempo 
		Timer timer = new Timer(); //administra los tiempo de repeticion
		public int hrs,min,seg;
		public boolean parar,first;
		TextField tf;
			public CronometroA(){
				first=true;
				parar=false;
			}
			class MiTarea extends TimerTask {
				public void run() {
					if(!parar){
						lTiempoLlevado++;//se aumenta el tiempo que lleva la reproduccion
						lTiempoRestante--;//se disminuye el tiempo restante 
						insertaTiempo();//se llama a la funcion inserta tiempo explicada debajo
						if(lTiempoRestante<-5) {//se ajusta el margen de 5 segundos que tarda en cargar el video
							parar = true;
						}
					}
				}
			}
			public void start(){
					if (first){
						try{
							parar= false;
							first = false;
							hrs=min=seg=0;
							// le asignamos una tarea al timer
							timer.schedule(new MiTarea(),0,1000);//y se ajusta el tiempo
							first=false;
						}	
						catch(Exception e){
							System.out.println("Error en el Timer (start)");
						}
					}
					else {
						try{
							parar= true;
							first = false;
							//hrs=min=seg=0;
							parar = false;
						}	
						catch(Exception e){
							System.out.println("Error en el Timer (start)");
						}
					}
				}
			
				public void stop() {
					try{
						parar= true;
					}catch(Exception e){
						System.out.println("Error en el Timer (stop)");
					}
				}
	}
	public void insertaTiempo(){ //funcion que unicamente se encarga de mantener los JLabel siempre actualizados, por eso mismo es llamada en el timer
		tiempoLlevado.setText(calculaTiempo(lTiempoLlevado));
		tiempoRestante.setText(calculaTiempo(lTiempoRestante));
	}
}
