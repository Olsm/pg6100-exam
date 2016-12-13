package org.pg6100.quizApi;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.pg6100.quizApi.collection.ListDto;
import org.pg6100.quizApi.dto.QuizDTO;
import org.pg6100.quizApi.hal.HalLink;

import static org.junit.Assert.assertEquals;

public class ListDtoTest {

    private ObjectMapper mapper = new ObjectMapper();


    @Test
    public void testBase() throws Exception {

        String href = "someHref";

        ListDto<QuizDTO> list = new ListDto<>();
        list._links = new ListDto.ListLinks();
        list._links.next = new HalLink(href);

        String json = mapper.writeValueAsString(list);
        System.out.println(json);

        ListDto<QuizDTO> back = mapper.readValue(json, ListDto.class);
        assertEquals(href, back._links.next.href);
    }
}