package com.ecommerce.sb_ecom.payload.response;

import com.ecommerce.sb_ecom.model.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryResponse {
    private List<Category> content;
}
