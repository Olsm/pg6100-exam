package org.pg6100.quizImp.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import org.pg6100.quizApi.api.RootCategoryRestApi;
import org.pg6100.quizImp.dto.CategoryConverter;
import org.pg6100.quizApi.dto.CategoryDTO;
import org.pg6100.quizApi.dto.SubCategoryDTO;
import org.pg6100.quizImp.businesslayer.CategoryEJB;
import org.pg6100.quizImp.businesslayer.QuizEJB;
import org.pg6100.quizImp.datalayer.Category;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.util.Set;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED) //avoid creating new transactions
public class RootCategoryRestImpl implements RootCategoryRestApi {

    @EJB
    private CategoryEJB cEJB;
    @EJB
    private QuizEJB qEJB;

    @Override
    public Set<CategoryDTO> get(boolean expand) {
        if (expand)
            return CategoryConverter.transformCatsWithSubCats(cEJB.getAllRootCategories());
        else
            return CategoryConverter.transformCategories(cEJB.getAllRootCategories());
    }

    @Override
    public CategoryDTO getRootCategoryById(Long id, boolean expand) {
        requireRootCategory(id);
        if (expand)
            return CategoryConverter.transformWithSubCats(cEJB.getRootCategory(id));
        else
            return CategoryConverter.transform(cEJB.getRootCategory(id));
    }

    @Override
    public Long createRootCategory(CategoryDTO dto) {
        if (dto.name == null)
            throw new WebApplicationException("Category name must be specified when creating root category");

        Category rootCategory;
        try {
            rootCategory = cEJB.registerRootCategory(dto.name);
        } catch (Exception e) {
            throw wrapException(e);
        }

        return rootCategory.getId();
    }

    @Override
    public void updateRootCategory(Long id, CategoryDTO dto) {
        if (! cEJB.rootCatExists(id))
            throw new WebApplicationException("Cannot find category with id " + id, 404);

        try {
            cEJB.updateRootCategory(id, dto.name);
        } catch (Exception e) {
            throw wrapException(e);
        }
    }

    @Override
    public void updateRootCategory(Long id, String jsonPatch) {
        if (! cEJB.rootCatExists(id))
            throw new WebApplicationException("Cannot find category with id " + id, 404);

        Category category = cEJB.getRootCategory(id);
        String name = category.getName();

        ObjectMapper jackson = new ObjectMapper();
        JsonNode jsonNode;
        try {
            jsonNode = jackson.readValue(jsonPatch, JsonNode.class);
        } catch (Exception e) {
            throw new WebApplicationException("Invalid JSON data as input: " + e.getMessage(), 400);
        }

        if (jsonNode.has("name")) {
            JsonNode node = jsonNode.get("name");
            if (node.isNull()) {
                name = null;
            } else if (node.isTextual()) {
                name = node.asText();
            } else {
                throw new WebApplicationException("Invalid JSON. Non-string name", 400);
            }
        }

        try {
            cEJB.updateRootCategory(id, name);
        } catch (Exception e) {
            throw wrapException(e);
        }
    }

    @Override
    public void deleteRootCategory(Long id) {
        cEJB.deleteRootCategory(id);
    }

    //----------------------------------------------------------

    private void requireRootCategory(Long id) throws WebApplicationException {
        if (!cEJB.rootCatExists(id)) {
            throw new WebApplicationException("Cannot find root category with id " + id, 404);
        }
    }

    private WebApplicationException wrapException(Exception e) throws WebApplicationException{

        /*
            Errors:
            4xx: the user has done something wrong, eg asking for something that does not exist (404)
            5xx: internal server error (eg, could be a bug in the code)
         */

        Throwable cause = Throwables.getRootCause(e);
        if(cause instanceof ConstraintViolationException){
            return new WebApplicationException("Invalid constraints on input: "+cause.getMessage(), 400);
        } else {
            return new WebApplicationException("Internal error", 500);
        }
    }


    /* Deprecated methods */

    @Override
    public Response deprecatedGetSubCategoriesByRootCategory(Long id) {
        return Response.status(301)
                .location(UriBuilder.fromUri("/subcategories")
                        .queryParam("parentId", id).build()).build();
    }

    @Override
    public Response deprecatedCreateSubCategoryByParent(Long id, SubCategoryDTO dto) {
        return Response.status(301)
                .location((UriBuilder.fromUri("/subcategories")
                        .queryParam("parentId", id).queryParam("dto", dto)
                        .build())).build();
    }

}
