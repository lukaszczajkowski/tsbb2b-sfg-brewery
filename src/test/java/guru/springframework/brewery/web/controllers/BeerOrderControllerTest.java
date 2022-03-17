package guru.springframework.brewery.web.controllers;

import guru.springframework.brewery.services.BeerOrderService;
import guru.springframework.brewery.web.model.BeerOrderDto;
import guru.springframework.brewery.web.model.BeerOrderPagedList;
import guru.springframework.brewery.web.model.OrderStatusEnum;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.reset;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.hamcrest.core.Is.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BeerOrderController.class)
class BeerOrderControllerTest {

    private static final UUID CUSTOMER_ID = UUID.randomUUID();
    private static final UUID ORDER_ID = UUID.randomUUID();

    @MockBean
    BeerOrderService beerOrderService;

    @Autowired
    MockMvc mockMvc;

    BeerOrderDto beerOrderDto;

    @BeforeEach
    void setUp() {
        beerOrderDto = BeerOrderDto.builder()
                .id(ORDER_ID)
                .beerOrderLines(new ArrayList<>())
                .customerId(CUSTOMER_ID)
                .createdDate(OffsetDateTime.now())
                .customerRef("12345")
                .orderStatus(OrderStatusEnum.NEW)
                .lastModifiedDate(OffsetDateTime.now())
                .version(1)
                .orderStatusCallbackUrl("https://www.google.com")
                .build();
    }

    @AfterEach
    void tearDown() {
        reset(beerOrderService);
    }

    @Test
    void listOrders() throws Exception {
        List<BeerOrderDto> beerOrderDtoList = List.of(beerOrderDto);
        BeerOrderPagedList beerOrderPagedList = new BeerOrderPagedList(beerOrderDtoList, PageRequest.of(1, 1), 2L);
        given(beerOrderService.listOrders(any(), any())).willReturn(beerOrderPagedList);

        mockMvc.perform(get("/api/v1/customers/{customerId}/orders", CUSTOMER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id", is(beerOrderDtoList.get(0).getId().toString())));
    }

    @Test
    void getOrder() throws Exception {
        given(beerOrderService.getOrderById(CUSTOMER_ID, ORDER_ID)).willReturn(beerOrderDto);

        mockMvc.perform(get("/api/v1/customers/{customerId}/orders/{orderId}", CUSTOMER_ID, ORDER_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.id", is(beerOrderDto.getId().toString())))
                .andExpect(jsonPath("$.customerRef", is(beerOrderDto.getCustomerRef())));
    }
}