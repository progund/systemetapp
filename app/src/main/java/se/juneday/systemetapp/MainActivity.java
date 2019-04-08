package se.juneday.systemetapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import se.juneday.systemetapp.domain.Product;

public class MainActivity extends AppCompatActivity {

  private static final String MIN_ALCO = "min_alcohol";;
  private static final String MAX_ALCO = "max_alcohol";;
  private static final String MIN_PRICE = "min_pricel";;
  private static final String MAX_PRICE = "max_price";;
  private static final String TYPE = "product_group";
  private static final String NAME = "name";

  private ViewStateCache viewCache;

  private static final String LOG_TAG = MainActivity.class.getSimpleName();
  private List<Product> products;
  private ListView listView;
  private ArrayAdapter<Product> adapter;
  private List<String> types;
//  private Map<String, String> searchMap;

  private void createFakedProducts() {
    products = new ArrayList<>();
    Product p1 = new Product.Builder()
        .alcohol(4.4)
        .name("Pilsner Urquell")
        .nr(1234)
        .productGroup("Öl")
        .type("Öl")
        .volume(330).build();
    Product p2 = new Product.Builder()
        .alcohol(4.4)
        .name("Baron Trenk")
        .nr(1234)
        .productGroup("Öl")
        .type("Öl")
        .volume(330).build();
      products.add(p1);
      products.add(p2);
  }

  private void setupListView() {
    // look up a reference to the ListView object
    listView = findViewById(R.id.product_list);

    // create an adapter (with the faked products)
    adapter = new ArrayAdapter<Product>(this,
        android.R.layout.simple_list_item_1,
        products);

    // Set listView's adapter to the new adapter
    listView.setAdapter(adapter);

    listView.setOnItemClickListener(new ListView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent,
          final View view,
          int position /*The position of the view in the adapter.*/,
          long id /* The row id of the item that was clicked */) {
        Log.d(LOG_TAG, "item clicked, pos:" + position + " id: " + id);
        Product p = products.get(position);
//        Intent intent = new Intent(MainActivity.this, ProductActivity.class);
  //      intent.putExtra("product", p);
        String url = "https://www.systembolaget.se/dryck/" + p.type().toLowerCase().replace("ö","o") + "/" + p.name().toLowerCase().split(" ")[0] + "-" + p.nr();
        Log.d(LOG_TAG, " url: " + url);
        Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(myIntent);
        //        startActivity(intent);
      }
    });
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    initTypes();

    initCache();

    // set up faked products
    createFakedProducts();

    // setup listview (and friends)
    setupListView();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.actionbar_menu, menu);

    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      // action with ID action_refresh was selected
      case R.id.action_search:
        Log.d(LOG_TAG, "user presssed SEARCH");
        showSearchDialog();
        break;
      default:
        Log.d(LOG_TAG, "uh oh ;)");
        break;
    }
    return true;
  }

  // get the entered text from a view
  private String valueFromView(View inflated, int viewId) {
    return ((EditText) inflated.findViewById(viewId)).getText().toString();
  }

  // if the value is valid, add it to the map
  private void addToMap(Map<String, String> map, String key, String value) {
    if (value!=null && !value.equals("")) {
      map.put(key, value);
    }
  }

  private void valueToView(View inflated, int viewId, Map<String, String> map, String key) {
    Log.d(LOG_TAG, "valueToView()");
    String text = (String) map.get(key);
    Log.d(LOG_TAG, "valueToView()   text: " + text);
    if (text!=null) {
      ((EditText) inflated.findViewById(viewId)).setText(text);
    }
  }

  private void showSearchDialog() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle("Search products");
    final View viewInflated = LayoutInflater
        .from(this).inflate(R.layout.search_dialog, null);


    builder.setView(viewInflated);
    Log.d(LOG_TAG, "spinner types: " + types.size() + " : " + types);
    final Spinner spinner = (Spinner) viewInflated.findViewById(R.id.product_type);
    Log.d(LOG_TAG, "spinner: " + spinner);
    ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this,
        android.R.layout.simple_spinner_item, types);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    spinner.setAdapter(adapter);

    viewCache.useValue(viewInflated);
/*    if (searchMap!=null) {
      Log.d(LOG_TAG, "Re-enter search values: " + searchMap);
      valueToView(viewInflated, R.id.min_alco_input, searchMap, MIN_ALCO);
      valueToView(viewInflated, R.id.max_alco_input, searchMap, MAX_ALCO);
      valueToView(viewInflated, R.id.min_price_input, searchMap, MIN_PRICE);
      valueToView(viewInflated, R.id.max_price_input, searchMap, MAX_PRICE);
      valueToView(viewInflated, R.id.product_name, searchMap, NAME);

      Log.d(LOG_TAG, " set type?");
      String type = searchMap.get(TYPE);
      Log.d(LOG_TAG, " set type, type: " + type);
      int pos = types.indexOf(URLDecoder.decode(type));
      Log.d(LOG_TAG, " set type, pos: " + pos);
      if (pos>=0) {
        spinner.setSelection(pos);
      }
    }
*/


    builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        dialog.dismiss();
        // Create a map to pass to the search method
        // The map makes it easy to add more search parameters with no changes in method signatures

        // Add user supplied argument (if valid) to the map
/*        addToMap(arguments, MIN_ALCO, valueFromView(viewInflated, R.id.min_alco_input));
        addToMap(arguments, MAX_ALCO, valueFromView(viewInflated, R.id.max_alco_input));
        addToMap(arguments, MIN_PRICE, valueFromView(viewInflated, R.id.min_price_input));
        addToMap(arguments, MAX_PRICE, valueFromView(viewInflated, R.id.max_price_input));
        addToMap(arguments, NAME, valueFromView(viewInflated, R.id.product_name));
        if (!spinner.getSelectedItem().toString().equals("Alla")) {
          arguments.put(TYPE, URLEncoder.encode(spinner.getSelectedItem().toString()));
        }
        */
        viewCache.cacheValues(viewInflated);

        // Given the map, s earch for products and update the listview
        searchProducts(createMapFromView(viewInflated));
      }
    });
    builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        Log.d(LOG_TAG, " User cancelled search");
        dialog.cancel();
      }
    });
    builder.show();
  }

  private Map<String, String> createMapFromView(View viewInflated) {
    Map<String, String> arguments= new HashMap<>();
    final Spinner spinner = (Spinner) viewInflated.findViewById(R.id.product_type);

    addToMap(arguments, MIN_ALCO, valueFromView(viewInflated, R.id.min_alco_input));
    addToMap(arguments, MAX_ALCO, valueFromView(viewInflated, R.id.max_alco_input));
    addToMap(arguments, MIN_PRICE, valueFromView(viewInflated, R.id.min_price_input));
    addToMap(arguments, MAX_PRICE, valueFromView(viewInflated, R.id.max_price_input));
    addToMap(arguments, NAME, valueFromView(viewInflated, R.id.product_name));
    if (!spinner.getSelectedItem().toString().equals("Alla")) {
      arguments.put(TYPE, URLEncoder.encode(spinner.getSelectedItem().toString()));
    }

    return arguments;
  }

  private void searchProducts(Map<String, String> arguments) {
    // empty search string will give a lot of products :)
    String argumentString = "";

    // iterate over the map and build up a string to pass over the network
    for (Map.Entry<String, String> entry : arguments.entrySet())
    {
      // If first arg use "?", otherwise use "&"
      // E g:    ?min_alcohol=4.4&max_alcohol=5.4
      argumentString += (argumentString.equals("")?"?":"&")
          + entry.getKey()
          + "="
          + entry.getValue();
    }
    // print argument
    Log.d(LOG_TAG, " arguments: " + argumentString);

    // search for products later on :)
    // Add one for now
   /* Product p = new Product.Builder()
        .alcohol(4.4)
        .name("Budvar")
        .nr(1234)
        .productGroup("Öl")
        .type("Öl")
        .volume(330).build();
    products.add(p);
    adapter.notifyDataSetChanged();
    */

    RequestQueue queue = Volley.newRequestQueue(this);
    String url = "http://rameau.sandklef.com:9090/search/products/all/" + argumentString;
    Log.d(LOG_TAG, "Searching using url: " + url);
    JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
        Request.Method.GET,
        url,
        null,
        new Response.Listener<JSONArray>() {

          @Override
          public void onResponse(JSONArray array) {
            Log.d(LOG_TAG, "onResponse()");
            products.clear();
            products.addAll(jsonToProducts(array));
            Collections.sort(products, new Comparator<Product>() {
              @Override
              public int compare(Product first, Product other) {
                return first.name().compareTo(other.name());
              }
            });
            adapter.notifyDataSetChanged();
          }
        }, new Response.ErrorListener() {

      @Override
      public void onErrorResponse(VolleyError error) {
        Log.d(LOG_TAG, " cause: " + error.getCause());
      }
      });

    // Add the request to the RequestQueue.
    queue.add(jsonArrayRequest);
  }

  private List<Product> jsonToProducts(JSONArray array) {
    Log.d(LOG_TAG, "jsonToProducts()");
    List<Product> productList = new ArrayList<>();
    for (int i = 0; i < array.length(); i++) {
      try {
        Log.d(LOG_TAG, "jsonToProducts() i: " + i);
        JSONObject row = array.getJSONObject(i);
        String name = row.getString("name");
        double alcohol = row.getDouble("alcohol");
        double price = row.getDouble("price");
        int volume = row.getInt("volume");
        int nr = row.getInt("nr");
        String type = row.getString("product_group");
        Log.d(LOG_TAG, "jsonToProducts() type: " + type);

        Product m = new Product.Builder().
          name(name).
                alcohol(alcohol).
                price(price).
                volume(volume).
                type(type).
                nr(nr).
                build();
        productList .add(m);
        Log.d(LOG_TAG, " * " + m);
      } catch (JSONException e) {
        ; // is ok since this is debug
      }
    }
    return productList;
  }

  private void initCache() {
    viewCache = new ViewStateCache();
    viewCache.add(R.id.product_type, null);
    viewCache.add(R.id.product_name, null);
    viewCache.add(R.id.min_alco_input, null);
    viewCache.add(R.id.max_alco_input, null);
    viewCache.add(R.id.min_price_input, null);
    viewCache.add(R.id.max_price_input, null);
  }

  private void initTypes() {
    types = new ArrayList<>();
    types.add("Alla");
    types.add("Öl");
    types.add("Whisky");
    types.add("Calvados");
    types.add("Cognac");
    types.add("Alkoholfritt");
    types.add("Aniskryddad sprit");
    types.add("Aperitif");
    types.add("Armagnac");
    types.add("Bitter");
    types.add("Blå mousserande");
    types.add("Blanddrycker");
    types.add("Blå stilla");
    types.add("Brandy och Vinsprit");
    types.add("Cider");
    types.add("Drinkar och Cocktails");
    types.add("Fruktvin");
    types.add("Genever");
    types.add("Gin");
    types.add("Glögg och Glühwein");
    types.add("Grappa och Marc");
    types.add("Juldrycker");
    types.add("Kryddad sprit");
    types.add("Likör");
    types.add("Madeira");
    types.add("Mjöd");
    types.add("Montilla");
    types.add("Mousserande vin");
    types.add("Okryddad sprit");
    types.add("Övrig sprit");
    types.add("Övrigt starkvin");
    types.add("Portvin");
    types.add("Punsch");
    types.add("Röda");
    types.add("Rom");
    types.add("Rosé");
    types.add("Rosévin");
    types.add("Rött vin");
    types.add("Sake");
    types.add("Sherry");
    types.add("Smaksatt sprit");
    types.add("Smaksatt vin");
    types.add("Snaps");
    types.add("Sprit av frukt");
    types.add("Tequila och Mezcal");
    types.add("Vermouth");
    types.add("Vin av flera typer");
    types.add("Vita");
    types.add("Vitt vin");
  }


}
