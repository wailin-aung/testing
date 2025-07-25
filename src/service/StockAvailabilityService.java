package service;

import dao.IngredientDAO;
import dao.RecipeDAO;
import dao.StockDAO;
import model.Ingredient;
import model.Recipe;
import model.Stock;

import java.sql.SQLException;
import java.util.List;

public class StockAvailabilityService {
    private final IngredientDAO ingredientDAO;
    private final StockDAO stockDAO;
    private final RecipeDAO recipeDAO;

    public StockAvailabilityService() {
        this.ingredientDAO = new IngredientDAO();
        this.stockDAO = new StockDAO();
        this.recipeDAO = new RecipeDAO();
    }

    public void updateAllStockAvailability() throws SQLException {
        List<Stock> stockList = stockDAO.getAllStock();

        for (Stock stock : stockList) {
            updateStockAvailability(stock);
        }
    }

    public void updateStockAvailability(Stock stock) throws SQLException {
        List<Recipe> recipes = recipeDAO.getRecipesByStockId(stock.getId());

        if (recipes.isEmpty()) {
            stock.setDescription("No Recipe");
            stock.setMaxServings(0);
            stockDAO.updateStock(stock);
            return;
        }

        int minServings = Integer.MAX_VALUE;
        boolean canMake = true;

        for (Recipe recipe : recipes) {
            Ingredient ingredient = ingredientDAO.getIngredientById(recipe.getIngredientId());

            if (ingredient == null || ingredient.getQuantity() < recipe.getRequiredQuantity()) {
                canMake = false;
                break;
            }

            int possibleServings = (int) (ingredient.getQuantity() / recipe.getRequiredQuantity());
            minServings = Math.min(minServings, possibleServings);
        }

        if (canMake && minServings > 0) {
            stock.setDescription("Available");
            stock.setMaxServings(minServings);
        } else {
            stock.setDescription("Not Available");
            stock.setMaxServings(0);
        }

        stockDAO.updateStock(stock);
    }
}
