package nl.andrewlalis.threadripper;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import nl.andrewlalis.threadripper.engine.ParticleChamber;
import nl.andrewlalis.threadripper.engine.Vec2;
import nl.andrewlalis.threadripper.particle.Particle;
import nl.andrewlalis.threadripper.particle.ParticleFactory;

/**
 * Main application starting point.
 */
@Slf4j
public class ThreadRipperApplication extends Application {

	private final Canvas canvas;
	private final ParticleChamber chamber;

	public ThreadRipperApplication() {
		this.canvas = new Canvas(1000, 1000);
		this.chamber = new ParticleChamber(canvas);
		ParticleFactory factory = new ParticleFactory(
				10000.0,
				10000000.0,
				0,
				0,
				new Vec2(350, 250),
				new Vec2(450, 400),
				new Vec2(10, -2),
				new Vec2(25, 2)
		);
		for (int i = 0; i < 100; i++) {
			this.chamber.addParticle(factory.build());
		}

//		this.chamber.addParticle(new Particle(new Vec2(400, 298), new Vec2(19, 0), 100000.0, 0.0));
//		this.chamber.addParticle(new Particle(new Vec2(400, 300), new Vec2(20, 0), 10000000000.0, 0.0));
		this.chamber.addParticle(new Particle(new Vec2(400, 500), new Vec2(0, 0), 1000000000000000.0, 0.0));

//		ParticleFactory factory1 = new ParticleFactory(
//				1000000000.0,
//				10000000000000.0,
//				0,
//				0,
//				new Vec2(700, 700),
//				new Vec2(900, 900),
//				new Vec2(0, 0),
//				new Vec2(0, 0)
//		);
//		for (int i = 0; i < 500; i++) {
//			this.chamber.addParticle(factory1.build());
//		}

		Thread particleChamberThread = new Thread(chamber);
		particleChamberThread.setName("ParticleChamber");
		particleChamberThread.start();
	}

	public static void main(String[] args) {
		log.info("Starting ThreadRipper application.");
		Application.launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception {
		stage.setTitle("ThreadRipper");

		GraphicsContext gc = this.canvas.getGraphicsContext2D();

		gc.setFill(Color.GREEN);
		gc.fillOval(10, 60, 30, 30);

		Group root = new Group();
		root.getChildren().add(this.canvas);
		Scene scene = new Scene(root);
		stage.setScene(scene);
		stage.show();
	}

	@Override
	public void stop() throws Exception {
		this.chamber.setRunning(false);
		super.stop();
		System.exit(0);
	}
}
