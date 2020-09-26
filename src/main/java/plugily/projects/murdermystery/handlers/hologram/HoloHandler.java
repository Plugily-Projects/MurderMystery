package plugily.projects.murdermystery.handlers.hologram;

import com.gmail.filoghost.holographicdisplays.api.Hologram;

public class HoloHandler {

	private Hologram bowHologram;

	public void setBowHologram(Hologram bowHologram) {
		if (bowHologram == null) {
			this.bowHologram = null;
			return;
		}

		if (this.bowHologram != null && !this.bowHologram.isDeleted()) {
			this.bowHologram.delete();
		}

		this.bowHologram = bowHologram;
	}

	public Hologram getBowHologram() {
		return bowHologram;
	}
}
