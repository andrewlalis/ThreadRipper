package nl.andrewlalis.threadripper;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import nl.andrewlalis.threadripper.engine.ParticleChamber;
import nl.andrewlalis.threadripper.engine.Vec2;
import nl.andrewlalis.threadripper.particle.ParticleFactory;
import nl.andrewlalis.threadripper.render.ParticleChamberRenderer;

/**
 * Main application starting point.
 */
@Slf4j
public class ThreadRipperApplication extends Application {

	private final Canvas canvas;

	private final ParticleChamber chamber;
	private final Thread chamberThread;
	private final ParticleChamberRenderer renderer;
	private final Thread renderThread;

	public ThreadRipperApplication() {
		this.canvas = new Canvas(800, 800);
		this.chamber = new ParticleChamber();
		this.renderer = new ParticleChamberRenderer(chamber, canvas);


		ParticleFactory factory = new ParticleFactory(
				0.1, 100000000000000.0,
				0, 0,
				0.5, 5,
				new Vec2(0, 0), new Vec2(800, 800),
				new Vec2(-50, -50), new Vec2(50, 50)
		);
		for (int i = 0; i < 50; i++) {
			this.chamber.addParticle(factory.build());
		}

		this.chamberThread = new Thread(this.chamber);
		this.chamberThread.start();

		this.renderThread = new Thread(this.renderer);
		this.renderThread.start();
	}

	public static void main(String[] args) {
		log.info("Starting ThreadRipper application.");
		Application.launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception {
		stage.setTitle("ThreadRipper");

		BorderPane borderPane = new BorderPane();
		borderPane.setCenter(this.canvas);

		TextField simRateField = new TextField(Double.toString(this.chamber.getSimulationRate()));
		Button simRateUpdate = new Button("Apply");
		simRateUpdate.setOnMouseClicked(mouseEvent -> {
			double simRate = this.chamber.getSimulationRate();
			try {
				simRate = Double.parseDouble(simRateField.getText().trim());
			} catch (NumberFormatException e) {
				simRateField.clear();
			}
			this.chamber.setSimulationRate(simRate);
		});
		HBox bottomPanel = new HBox(
				new Label("Simulation Rate"),
				simRateField,
				simRateUpdate
		);
		borderPane.setBottom(bottomPanel);

		Scene scene = new Scene(borderPane);

		ChangeListener<Number> sceneSizeListener = (observable, oldValue, newValue) -> {
			this.canvas.setWidth(scene.getWidth());
			this.canvas.setHeight(scene.getHeight());
		};
		scene.widthProperty().addListener(sceneSizeListener);
		scene.heightProperty().addListener(sceneSizeListener);

		stage.setScene(scene);
		stage.setMaximized(true);
		stage.show();
	}

	@Override
	public void stop() throws Exception {
		this.chamber.setRunning(false);
		this.chamberThread.join();
		this.renderer.setRunning(false);
		this.renderThread.join();
		super.stop();
	}
}
