package io.sim;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.tudresden.sumo.objects.SumoStringList;
import de.tudresden.sumo.util.SumoCommand;

public class Route {
    private String id;
    private SumoStringList edges;

    public Route(String id, SumoStringList sumoStringList) {
        this.id = id;
        this.edges = sumoStringList;
    }

    public String getId() {
        return id;
    }

    public SumoStringList getEdges() {
        return edges;
    }

   public static SumoCommand getEdges(String routeID) {
      return new SumoCommand(166, 84, routeID, 182, 14);
   }

   public static SumoCommand getIDList() {
      return new SumoCommand(166, 0, "", 182, 14);
   }

   public static SumoCommand getIDCount() {
      return new SumoCommand(166, 1, "", 182, 9);
   }

   public static SumoCommand getParameter(String routeID, String param) {
      Object[] array = new Object[]{param};
      return new SumoCommand(166, 126, routeID, array, 182, 12);
   }

   public static SumoCommand setParameter(String routeID, String param, String value) {
      Object[] array = new Object[]{param, value};
      return new SumoCommand(198, 126, routeID, array);
   }

   public static SumoCommand add(String routeID, SumoStringList edges) {
      return new SumoCommand(198, 128, routeID, edges);
   }

   public String toJson() {
      try {
         ObjectMapper mapper = new ObjectMapper();
         return mapper.writeValueAsString(this);
      } catch (Exception e) {
         System.out.println("erro ao converter Route>JSON");
         e.printStackTrace(); 
         return null;
      }
   }

   public static Route fromJson(String json) {
      try {
         ObjectMapper mapper = new ObjectMapper();
         return mapper.readValue(json, Route.class);
      } catch (Exception e) {
         System.out.println("erro ao converter JSON>Route");
         e.printStackTrace(); 
         return null;
      }
   }
}
