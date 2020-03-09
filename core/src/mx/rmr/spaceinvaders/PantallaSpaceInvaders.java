package mx.rmr.spaceinvaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

class PantallaSpaceInvaders extends Pantalla
{
    private final Juego juego;

    // Aliens
    private Array<Alien> arrAliens;
    private Texture texturaAlien;

    // Nave
    private Nave nave;
    private Texture texturaNave;
    private Movimiento movimiento = Movimiento.QUIETO;

    // Bala
    private Bala bala;      // null
    private Texture texturaBala;

    public PantallaSpaceInvaders(Juego juego) {
        this.juego = juego;
    }

    @Override
    public void show() {
        cargarTexturas();
        crearAliens();
        crearNave();

        Gdx.input.setInputProcessor(new ProcesadorEntrada());
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
        int COLUMNAS = 11;
        int RENGLONES = 5;
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
        moverNave();
        moverBala(delta);

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

        batch.end();
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
                    movimiento = Movimiento.IZQUIERDA;
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
}
