package mx.rmr.spaceinvaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.Viewport;

class PantallaSpaceInvaders extends Pantalla
{
    private final Juego juego;

    // Sistema de partículas
    private ParticleEffect sistemaParticulas;
    private ParticleEmitter emisorFuego;

    // Pausa
    private EscenaPausa escenaPausa;
    private EstadoJuego estadoJuego = EstadoJuego.JUGANDO;        // JUGANDO, PAUSADO, GANO, PERDIO, etc

    // Aliens
    private Array<Alien> arrAliens;
    private Texture texturaAlien;
    private float TIEMPO_PASO = 0.5f;
    private int MAX_PASOS = 32;
    private int numeroPasos = MAX_PASOS/2;  // Ya inician en el centro
    private Direccion direccion = Direccion.DERECHA;
    private float timerAlienMover = 0;
    private final int COLUMNAS = 11;
    private final int RENGLONES = 5;

    // Nave
    private Nave nave;
    private Texture texturaNave;
    private Movimiento movimiento = Movimiento.QUIETO;

    // Bala
    private Bala bala;      // null
    private Texture texturaBala;

    // Marcador
    private Marcador marcador;

    public PantallaSpaceInvaders(Juego juego) {
        this.juego = juego;
    }

    @Override
    public void show() {
        cargarTexturas();
        crearAliens();
        crearNave();
        crearMarcador();
        // Sistema de partículas
        crearParticulas();

        Gdx.input.setInputProcessor(new ProcesadorEntrada());
    }

    private void crearParticulas() {
        sistemaParticulas = new ParticleEffect();
        sistemaParticulas.load(Gdx.files.internal("fuego.p"),
                Gdx.files.internal(""));
        Array<ParticleEmitter> emisores = sistemaParticulas.getEmitters();
        emisorFuego = emisores.get(0);
        emisores.get(0).setPosition(ANCHO/2, ALTO/2);
        sistemaParticulas.start();
    }

    private void crearMarcador() {
        marcador = new Marcador(0.2f*ANCHO, 0.9f*ALTO);
    }

    private void crearNave() {
        nave = new Nave(texturaNave, ANCHO/2, ALTO*0.05f);
    }

    private void cargarTexturas() {
        texturaAlien = new Texture("space/enemigoArriba.png");
        texturaNave = new Texture("space/nave.png");
        texturaBala = new Texture("space/bala.png");
    }

    private void crearAliens() {

        arrAliens = new Array<>(RENGLONES*COLUMNAS);
        float dx = ANCHO*0.8f / COLUMNAS;
        float dy = ALTO*0.4f / RENGLONES;

        for (int x=0; x<COLUMNAS; x++) {
            for (int y=0; y<RENGLONES; y++) {
                // Crear el alien con Text, x, y
                Alien alien = new Alien(texturaAlien,
                        x*dx + ANCHO*0.1f, y*dy + ALTO*0.45f);
                arrAliens.add(alien);
            }
        }
    }


    @Override
    public void render(float delta) {
        //Gdx.app.log("DELTA", delta+" s");
        // ACTUALIZACIONES
        if (estadoJuego==EstadoJuego.JUGANDO) {
            actualizar(delta);
            sistemaParticulas.update(delta);        // Para aplicar física
        }

        // DIBUJAR
        borrarPantalla(0,0,0);
        batch.setProjectionMatrix(camara.combined);

        batch.begin();

        for (Alien alien : arrAliens) {
            alien.render(batch);
        }
        nave.render(batch);
        if (bala != null) {
            bala.render(batch);
        }

        // Marcador
        marcador.render(batch);

        sistemaParticulas.draw(batch);


        batch.end();

        if (estadoJuego == EstadoJuego.PAUSADO) {
            escenaPausa.draw();
        }
    }

    private void actualizar(float delta) {
        moverNave();
        moverBala(delta);
        moverEnemigos(delta);
        probarColisiones();
    }

    private void moverEnemigos(float delta) {
        timerAlienMover += delta;
        if (timerAlienMover>=TIEMPO_PASO) {
            timerAlienMover = 0;
            float paso = ANCHO * 0.2f / MAX_PASOS;
            // Operador ? :
            float pasoDir = direccion==Direccion.DERECHA?paso:-paso;
            for (Alien alien : arrAliens) {
                alien.mover(pasoDir);
            }

            numeroPasos++;
            if (numeroPasos>=MAX_PASOS) {
                if (direccion==Direccion.DERECHA) {
                    direccion = Direccion.IZQUIERDA;
                } else {
                    direccion = Direccion.DERECHA;
                }
                numeroPasos = 0;
                // Baja un paso
                float pasoAbajo = ALTO*0.4f / RENGLONES;
                for (Alien alien :
                        arrAliens) {
                    alien.bajar(pasoAbajo);
                }
            }
        }
    }

    // Prueba si la bala le pegó a un enemigo
    private void probarColisiones() {
        if (bala!=null) {
            // Se pueden borrar elementos
            for (int i = arrAliens.size - 1; i >= 0; i--) {
                Alien alien = arrAliens.get(i);
                Rectangle rectAlien = alien.sprite.getBoundingRectangle();
                Rectangle rectBala = bala.sprite.getBoundingRectangle();
                if (rectAlien.overlaps(rectBala)) {
                    arrAliens.removeIndex(i);
                    bala = null;
                    marcador.marcar(1);
                    break;
                }
            }
        }
    }

    private void moverBala(float delta) {
        if (bala!=null) {
            bala.mover(delta);
            // Salio?,..... colisionó
            if (bala.sprite.getY()>ALTO) {
                // Fuera de la pantalla
                bala = null;
            }
        }
    }

    private void moverNave() {
        switch (movimiento) {
            case DERECHA:
                nave.mover(10);
                break;
            case IZQUIERDA:
                nave.mover(-10);
                break;

                default:
                    break;
        }
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {
        texturaAlien.dispose();
        texturaBala.dispose();
        texturaNave.dispose();
        sistemaParticulas.dispose();
        escenaPausa.dispose();
    }

    private class ProcesadorEntrada implements InputProcessor
    {
        @Override
        public boolean keyDown(int keycode) {
            return false;
        }

        @Override
        public boolean keyUp(int keycode) {
            return false;
        }

        @Override
        public boolean keyTyped(char character) {
            return false;
        }

        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            Vector3 v = new Vector3(screenX, screenY, 0);
            camara.unproject(v);

            // Disparo????
            if (v.y<ALTO/2) {
                // Disparo!!!!!!
                if (bala == null) {
                    float xBala = nave.sprite.getX() + nave.sprite.getWidth()/2
                            - texturaBala.getWidth()/2;
                    float yBala = nave.sprite.getY()+nave.sprite.getHeight();
                    bala = new Bala(texturaBala, xBala, yBala);
                }
            } else {
                if (v.x >= ANCHO / 2) {
                    // Derecha
                    movimiento = Movimiento.DERECHA;
                } else {
                    // Izquierda
                    //movimiento = Movimiento.IZQUIERDA;
                    //PAUSAR EL JUEGO
                    estadoJuego = EstadoJuego.PAUSADO;
                    if (escenaPausa==null) {
                        escenaPausa = new EscenaPausa(vista, batch);
                    }
                }
            }
            return true;
        }

        @Override
        public boolean touchUp(int screenX, int screenY, int pointer, int button) {
            movimiento = Movimiento.QUIETO;
            return true;
        }

        @Override
        public boolean touchDragged(int screenX, int screenY, int pointer) {
            return false;
        }

        @Override
        public boolean mouseMoved(int screenX, int screenY) {
            return false;
        }

        @Override
        public boolean scrolled(int amount) {
            return false;
        }
    }

    // Movimiento
    public enum Movimiento
    {
        DERECHA,
        IZQUIERDA,
        QUIETO
    }

    // Clase Pause (ventana que se muestra cuando el usuario pausa la app)
    class EscenaPausa extends Stage
    {
        public EscenaPausa(Viewport vista, SpriteBatch batch) {
            super(vista, batch);

            Pixmap pixmap = new Pixmap((int)(ANCHO*0.7f), (int)(ALTO*0.8f),
                    Pixmap.Format.RGBA8888);
            pixmap.setColor(0, 0.5f, 0, 0.5f);
            pixmap.fillCircle(300,300, 300);
            Texture texturaCirculo = new Texture(pixmap);

            Image imgCirculo = new Image(texturaCirculo);
            imgCirculo.setPosition(ANCHO/2 - pixmap.getWidth()/2,
                    ALTO/2 - pixmap.getHeight()/2);

            this.addActor(imgCirculo);
        }
    }

    private enum EstadoJuego
    {
        JUGANDO,
        PAUSADO,
        GANO,
        PERDIO
    }
}









