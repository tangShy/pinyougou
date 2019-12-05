package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.pojo.Result;
import com.pinyougou.pojogroup.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import util.CookieUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {
    @Reference
    private CartService cartService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private HttpServletResponse response;

    /**
     * 购物车列表
     *
     * @return
     */
    @RequestMapping("/findCartList")
    public List<Cart> findCartList() {
        String cartListString = CookieUtil.getCookieValue(request,"cartList","UTF-8");
        if(cartListString == null || cartListString.equals("")){
            cartListString = "[]";
        }
        List<Cart> cartList_cookie = JSON.parseArray(cartListString, Cart.class);
        return cartList_cookie;
    }

    /**
     * 添加商品到购物车
     *
     * @param itemId
     * @param num
     * @return
     */
    @RequestMapping("/addGoodsToCartList")
    public Result addGoodsToCartList(Long itemId, Integer num){
        System.out.println(itemId + "---" + num);
        try {
            List<Cart> cartList = findCartList(); //获取购物车列表
            cartList = cartService.addGoodsToCartList(cartList, itemId, num);
            CookieUtil.setCookie(request, response, "cartList", JSON.toJSONString(cartList), 3600*24, "UTF-8");
            return new Result(true, "添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "添加失败");
        }
    }
}
