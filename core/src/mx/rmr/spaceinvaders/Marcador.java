package mx.rmr.spaceinvaders;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

class Marcador
{
    private int puntos;

    private float x;
    private float y;

    private Texto texto;    // Desplegar el texto "Puntos: 250"

    // Constructor
    public Marcador(float x, float y) {
        this.x = x;
        this.y = y;
        this.puntos = 0;
        this.texto = new Texto("fuente.fnt");    // Fuente
    }

    public void reset() {
        puntos = 0;
    }

    // Agrega puntos
    public void marcar(int puntos) {
        this.puntos += puntos;
    }

    public void render(SpriteBatch batch) {
        String mensaje = "puntos: " + puntos;
        texto.render(batch, mensaje, x, y);
    }
}
