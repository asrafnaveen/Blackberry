package bundle.android.model.tasks;

import java.util.List;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

import android.os.AsyncTask;

public class LoadMapItems {/*extends AsyncTask<Integer,Integer,List<ItemizedOverlay>> {

	@Override
	protected List<ItemizedOverlay> doInBackground(Integer... arg0) {
		// TODO Auto-generated method stub

		int left = arg0[0];
		int top = arg0[1];
		int right = arg0[2];
		int bottom = arg0[3];

		return convertToItemizedOverlay( someService.loadSomething( left, top, right, bottom ) );
	}

	private List<ItemizedOverlay> convertToItemizedOverlay( List<SomeObject> objects ) {
		// ... fill this out to convert your back end object to overlay items
	}

	public void onPostExecute(List<ItemizedOverlay> items) {
		List<Overlay> mapOverlays = mapView.getOverlays();
		for( ItemizedOverlay item : items ) {
			mapOverlays.add( item );
		}
	}

	private MapView view;

	public LoadMapItems( MapView view ) {
		this.view = view;
	}*/

}
