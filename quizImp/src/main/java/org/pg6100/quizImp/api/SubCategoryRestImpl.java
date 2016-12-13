package org.pg6100.quizImp.api;

import com.google.common.base.Throwables;
import org.pg6100.quizApi.api.SubCategoryRestApi;
import org.pg6100.quizImp.dto.CategoryConverter;
import org.pg6100.quizApi.dto.SubCategoryDTO;
import org.pg6100.quizImp.businesslayer.CategoryEJB;
import org.pg6100.quizImp.businesslayer.QuizEJB;
import org.pg6100.quizImp.datalayer.SubCategory;

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
public class SubCategoryRestImpl implements SubCategoryRestApi {

    @EJB
    private CategoryEJB cEJB;
    @EJB
    private QuizEJB qEJB;

    @Override
    public Set<SubCategoryDTO> get(Long parentId) {
        if (parentId != null)
            return CategoryConverter.transformSubCategories(cEJB.getRootCategory(parentId).getSubCategoryList());
        else
            return  CategoryConverter.transformSubCategories(cEJB.getAllSubCategories());
    }

    @Override
    public SubCategoryDTO getSubCategoryById(Long id) {
        requireSubCategory(id);
        return CategoryConverter.transform(cEJB.getSubCategory(id));
    }

    @Override
    public Long createSubCategory(SubCategoryDTO dto, Long rootId) {
        if (rootId != null)
            dto.rootCategoryId = rootId.toString();
        if (dto.rootCategoryId == null)
            throw new WebApplicationException("Root category must be specified when creating sub category");
        else if (dto.name == null)
            throw new WebApplicationException("Category name must be specified when creating sub category");

        SubCategory subCategory;
        try {
            long rootCatId = parseId(dto.rootCategoryId);
            subCategory = cEJB.registerSubCategory(cEJB.getRootCategory(rootCatId), dto.name);
        } catch (Exception e) {
            throw wrapException(e);
        }

        return subCategory.getId();
    }

    @Override
    public void updateSubCategory(Long id, SubCategoryDTO dto) {
        long rootCatId = parseId(dto.rootCategoryId);
        requireRootCategory(rootCatId);
        requireSubCategory(id);

        try {
            cEJB.updateSubCategory(id, dto.name, rootCatId);
        } catch (Exception e) {
            throw wrapException(e);
        }
    }

    @Override
    public void deleteSubCategory(Long id) {
        cEJB.deleteSubCategory(id);
    }

    //----------------------------------------------------------

    private void requireRootCategory(Long id) throws WebApplicationException {
        if (!cEJB.rootCatExists(id)) {
            throw new WebApplicationException("Cannot find root category with id " + id, 404);
        }
    }

    private void requireSubCategory(Long id) throws WebApplicationException {
        if (!cEJB.subCatExists(id)) {
            throw new WebApplicationException("Cannot find sub category with id " + id, 404);
        }
    }

    private long parseId(String id) {
        try{
            return Long.parseLong(id);
        } catch (Exception e){
            throw new WebApplicationException("Invalid id: " + id, 400);
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

}
