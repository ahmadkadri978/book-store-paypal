package book.store.controller;
import book.store.entity.Book;
import book.store.service.BookService;
import book.store.service.PayPalService;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

@Controller
public class BookController {

    @Autowired
    private BookService bookService;

    @Autowired
    private PayPalService payPalService;

    @Value("${paypal.success.url}")
    private String successUrl;

    @Value("${paypal.cancel.url}")
    private String cancelUrl;

    @GetMapping("/")
    public String viewHomePage(Model model) {
        model.addAttribute("books", bookService.getAllBooks());
        return "index";
    }

    @PostMapping("/purchase/{id}")
    public String purchaseBook(@PathVariable Long id) {
        Book book = bookService.getBookById(id);
        try {
            Payment payment = payPalService.createPayment(
                    book.getPrice(),
                    "EUR",
                    "paypal",
                    "sale",
                    book.getTitle(),
                    cancelUrl,
                    successUrl + "/" + book.getId()
            );
            for (com.paypal.api.payments.Links links : payment.getLinks()) {
                if (links.getRel().equals("approval_url")) {
                    return "redirect:" + links.getHref();
                }
            }
        } catch (PayPalRESTException e) {
            e.printStackTrace();
        }
        return "redirect:/";
    }

    @GetMapping("/success/{id}")
    public String successPay(@PathVariable Long id, @RequestParam("paymentId") String paymentId, @RequestParam("PayerID") String payerId , Model model) {
        try {
            Payment payment = payPalService.executePayment(paymentId, payerId);
            if (payment.getState().equals("approved")) {
                model.addAttribute("bookId", id);

                return "download";
            }
        } catch (PayPalRESTException e) {
            e.printStackTrace();
        }
        return "redirect:/";
    }

    @GetMapping("/download/{id}")
    public void downloadBook(@PathVariable Long id, HttpServletResponse response) throws IOException {

        Book book = bookService.getBookById(id);
        if (book == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }


        File file = new File(book.getFilePath());
        System.out.println("Attempting to download file: " + file.getAbsolutePath());

        if (file.exists()) {
            System.out.println("im here");
            response.setContentType("application/pdf");
            response.addHeader("Content-Disposition", "attachment; filename=" + file.getName());

            try (FileInputStream fileInputStream = new FileInputStream(file);
                 OutputStream outputStream = response.getOutputStream()) {


                int read;
                byte[] buffer = new byte[1024];

                while ((read = fileInputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, read);
                }
                System.out.println("im here");
                outputStream.flush(); // Ensure all data is written out


            } catch (IOException e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

}
